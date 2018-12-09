package com.rain.networkproxy;

import android.support.annotation.NonNull;

import com.rain.networkproxy.helper.NPLogger;
import com.rain.networkproxy.model.Instruction;
import com.rain.networkproxy.model.NPState;
import com.rain.networkproxy.model.PendingResponse;
import com.rain.networkproxy.model.RequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

final class NPInterceptor implements Interceptor {
    private final NPProcess process;

    NPInterceptor(NPProcess process) {
        if (!process.isRunning()) {
            throw new IllegalStateException("Should call NetworkProxy.init() before using");
        }

        this.process = process;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        final Request request = chain.request();
        final Response response = chain.proceed(request);
        final RequestFilter requestFilter = process.currentState().getRequestFilter();

        if (!requestFilter.isMatch(request.url().encodedPath())) {
            return response;
        }

        return waitAndApplyInstruction(response);
    }

    private Response waitAndApplyInstruction(Response response) {
        final String id = UUID.randomUUID().toString();
        final PendingResponse pendingResponse = new PendingResponse(id, response);
        process.dispatch(new NPCommand.AddPendingResponse(pendingResponse));

        final Instruction instruction = process.state()
                .map(new Function<NPState, Instruction>() {
                    @Override
                    public Instruction apply(NPState state) {
                        return findInstruction(id, state.getInstructions());
                    }
                })
                .filter(new Predicate<Instruction>() {
                    @Override
                    public boolean test(Instruction instruction) {
                        return !instruction.isEmpty();
                    }
                })
                .blockingFirst();

        try {
            return apply(response, instruction);
        } catch (InterruptedException e) {
            NPLogger.logError("Interceptor with id: " + id, e);
            return response;
        }
    }

    @NonNull
    private Response apply(Response response, Instruction instruction) throws InterruptedException {
        final Instruction.Input input = instruction.getInput();
        final Response.Builder builder = response.newBuilder();

        if (input.getStatus() != null) {
            builder.code(input.getStatus());
        }

        if (input.getDelay() != null) {
            Thread.sleep(input.getDelay());
        }

        return builder.build();
    }

    @NonNull
    private Instruction findInstruction(String id, List<Instruction> instructions) {
        for (Instruction instruction : instructions) {
            if (instruction.getId().equals(id)) {
                return instruction;
            }
        }

        return Instruction.EMPTY;
    }
}
