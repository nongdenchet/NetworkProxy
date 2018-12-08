package com.rain.example

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.Toast
import com.rain.example.model.Todo
import com.rain.example.model.TodoApi
import kotlinx.android.synthetic.main.activity_todo.btnLoadMore
import kotlinx.android.synthetic.main.activity_todo.pbPhotos
import kotlinx.android.synthetic.main.activity_todo.rvPhotos
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class TodoActivity : AppCompatActivity() {
    private val service = createService()
    private val todoList = mutableListOf<Todo>()
    private val todoAdapter = TodoAdapter()
    private var loadTask: Call<Todo>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_todo)
        initView()
        loadTodo()
    }

    private fun initView() {
        rvPhotos.adapter = todoAdapter
        rvPhotos.layoutManager = LinearLayoutManager(this)
        btnLoadMore.setOnClickListener { loadTodo() }
    }

    private fun createService(): TodoApi {
        val client = OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor())
                .build()

        val retrofit = Retrofit.Builder()
                .baseUrl("https://jsonplaceholder.typicode.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()

        return retrofit.create<TodoApi>(TodoApi::class.java)
    }

    private fun currentId(): Int {
        return if (todoList.isEmpty()) 1
        else todoList.last().id + 1
    }

    private fun showLoading() {
        pbPhotos.visibility = View.VISIBLE
        btnLoadMore.visibility = View.INVISIBLE
    }

    private fun hideLoading() {
        pbPhotos.visibility = View.GONE
        btnLoadMore.visibility = View.VISIBLE
    }

    private fun loadTodo() {
        loadTask?.cancel()
        loadTask = service.getTodo(currentId())
        showLoading()
        loadTask?.enqueue(object : Callback<Todo> {
            override fun onFailure(call: Call<Todo>, err: Throwable) = handleError(err)
            override fun onResponse(call: Call<Todo>, response: Response<Todo>) = handleResponse(response)
        })
    }

    private fun handleError(err: Throwable) {
        hideLoading()
        Toast.makeText(this@TodoActivity, err.message, Toast.LENGTH_SHORT)
                .show()
    }

    private fun handleResponse(response: Response<Todo>) {
        hideLoading()
        response.body()?.run {
            todoList.add(this)
            todoAdapter.submitList(todoList)
            rvPhotos.smoothScrollToPosition(todoList.lastIndex)
        }
    }

    override fun onDestroy() {
        loadTask?.cancel()
        super.onDestroy()
    }
}
