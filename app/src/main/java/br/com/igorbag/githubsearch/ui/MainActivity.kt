package br.com.igorbag.githubsearch.ui

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import br.com.igorbag.githubsearch.R
import br.com.igorbag.githubsearch.data.GitHubService
import br.com.igorbag.githubsearch.domain.Repository
import br.com.igorbag.githubsearch.ui.adapter.RepositoryAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    lateinit var nomeUsuario: EditText
    lateinit var btnConfirmar: Button
    lateinit var listaRepositories: RecyclerView
    lateinit var githubApi: GitHubService
    lateinit var progressBar: ProgressBar
    lateinit var noInternetImage: ImageView

    //para armazenar preferências compartilhadas (SharedPreferences)
    private val PREFS_NAME = "MyPrefsFile"
    //armazenar e recuperar o nome de usuário nas preferências compartilhadas.
    private val USERNAME_KEY = "username"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupView()
        showUserName()
        setupRetrofit()
        setupListeners()
    }

    // Metodo responsavel por realizar o setup da view e recuperar os Ids do layout
    fun setupView() {
        //@TODO 1 - Recuperar os Id's da tela para a Activity com o findViewById
        nomeUsuario = findViewById(R.id.et_nome_usuario)
        btnConfirmar = findViewById(R.id.btn_confirmar)
        listaRepositories = findViewById(R.id.rv_lista_repositories)
        progressBar = findViewById(R.id.progressBar)
        noInternetImage = findViewById(R.id.iv_no_internet)
    }

    //Metodo responsavel por configurar os listeners click da tela
    private fun setupListeners() {
        //@TODO 2 - colocar a acao de click do botao confirmar
        btnConfirmar.setOnClickListener {
            if (isInternetAvailable()) {
                val nomeUsuarioDigitado = nomeUsuario.text.toString()
                // Mostra a ProgressBar antes de fazer a chamada à API (Carregando)
                saveUserLocal(nomeUsuarioDigitado)
                noInternetImage.isVisible = false
                progressBar.visibility = View.VISIBLE
                getAllReposByUserName(nomeUsuarioDigitado)
            } else {
                // Exibe uma mensagem ao usuário informando que não há conexão com a internet
                Toast.makeText(this, "Sem conexão com a internet", Toast.LENGTH_SHORT).show()
                noInternetImage.isVisible = true
            }
        }
    }

    //Verificar se há internet
    private fun isInternetAvailable(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    // Salvar o usuario preenchido no EditText utilizando uma SharedPreferences
    private fun saveUserLocal(username: String) {
        //@TODO 3 - Persistir o usuario preenchido na editText com a SharedPref no listener do botao salvar
        val prefs: SharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = prefs.edit()
        editor.putString(USERNAME_KEY, username)
        editor.apply()
    }

    private fun showUserName() {
        //@TODO 4- depois de persistir o usuario exibir sempre as informacoes no EditText  se a sharedpref possuir algum valor, exibir no proprio editText o valor salvo
        val prefs: SharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedUsername = prefs.getString(USERNAME_KEY, "")
        if (savedUsername?.isNotEmpty() == true) {
            nomeUsuario.setText(savedUsername)
        }
    }

    //Metodo responsavel por fazer a configuracao base do Retrofit
    fun setupRetrofit() {
        /*
           @TODO 5 -  realizar a Configuracao base do retrofit
           Documentacao oficial do retrofit - https://square.github.io/retrofit/
           URL_BASE da API do  GitHub= https://api.github.com/
           lembre-se de utilizar o GsonConverterFactory mostrado no curso
        */
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        githubApi = retrofit.create(GitHubService::class.java)
    }

    //Metodo responsavel por buscar todos os repositorios do usuario fornecido
    fun getAllReposByUserName(username: String) {
        // TODO 6 - realizar a implementacao do callback do retrofit e chamar o metodo setupAdapter se retornar os dados com sucesso
        githubApi.getAllRepositoriesByUser(username).enqueue(object : Callback<List<Repository>> {
            override fun onResponse(call: Call<List<Repository>>, response: Response<List<Repository>>) {
                progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    val repositories = response.body()
                    setupAdapter(repositories ?: emptyList())
                } else {
                    showToast("Erro na resposta da API.")
                }
            }
            override fun onFailure(call: Call<List<Repository>>, t: Throwable) {
                progressBar.visibility = View.GONE
                showToast("Falha na chamada da API.")
            }
        })
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // Metodo responsavel por realizar a configuracao do adapter
    fun setupAdapter(list: List<Repository>) {
        /*
            @TODO 7 - Implementar a configuracao do Adapter , construir o adapter e instancia-lo
            passando a listagem dos repositorios
         */
        val adapter = RepositoryAdapter(list,this)
        listaRepositories.adapter = adapter
    }


    // Metodo responsavel por compartilhar o link do repositorio selecionado
    // @Todo 11 - Colocar esse metodo no click do share item do adapter
    fun shareRepositoryLink(urlRepository: String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, urlRepository)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    // Metodo responsavel por abrir o browser com o link informado do repositorio

    // @Todo 12 - Colocar esse metodo no click item do adapter
    fun openBrowser(urlRepository: String) {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(urlRepository)
            )
        )

    }

}