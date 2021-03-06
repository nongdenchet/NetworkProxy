package com.rain.networkproxy;

import android.support.annotation.NonNull;

import com.rain.networkproxy.model.Instruction;
import com.rain.networkproxy.model.PendingResponse;
import com.rain.networkproxy.model.RequestFilter;

import java.io.IOException;
import java.util.List;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

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
        final String url = request.url().url().toString();
        if (!requestFilter.isMatch(url)) {
            return response;
        }

        return waitAndApplyInstruction(response);
    }

    private Response waitAndApplyInstruction(Response response) {
        final String id = String.valueOf(process.getNextId());
        final PendingResponse pendingResponse = new PendingResponse(id, response);
        process.dispatch(new NPCommand.AddPendingResponse(pendingResponse));

        final Instruction instruction = process.state()
                .map(state -> findInstruction(id, state.getInstructions()))
                .filter(i -> !i.isEmpty())
                .blockingFirst();

        return apply(response, instruction);
    }

    @NonNull
    private Response apply(Response response, Instruction instruction) {
        final Instruction.Input input = instruction.getInput();
        final Response.Builder builder = response.newBuilder();

        if (input.getStatus() != null) {
            builder.code(input.getStatus());
        }

        final ResponseBody responseBody = response.body();
        if (input.getBody() != null && responseBody != null) {
            builder.body(ResponseBody.create(responseBody.contentType(), input.getBody()));
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
