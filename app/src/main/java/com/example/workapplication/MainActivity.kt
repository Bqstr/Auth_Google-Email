package com.example.workapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.findNavController
import com.example.workapplication.MainActivity.FirebaseUtils.firebaseUser
import com.example.workapplication.ui.theme.WorkApplicationTheme
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth

        setContent {
            WorkApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel = viewModel<SignInViewModel>()



                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "log_in") {
                        composable("sign_in") {
                            var email = rememberSaveable { mutableStateOf("") }
                            var password = rememberSaveable { mutableStateOf("") }

                            Box(Modifier.fillMaxSize()) {

                                Column(Modifier.align(Alignment.Center)) {
                                    Text(text = "Register" , fontSize = 24.sp, modifier = Modifier.padding(bottom  =20.dp))

                                    TextField(
                                        value = email.value,
                                        onValueChange = { email.value = it },
                                        modifier = Modifier.padding(bottom = 10.dp),
                                        label  = { Text("Email") }
                                    )
                                    TextField(
                                        value = password.value,
                                        onValueChange = { password.value = it },
                                                label  = { Text("Password") })
                                    Button(onClick = {
                                        signInUser(navController, email.value, password.value)
                                    } ,modifier = Modifier.align(
                                        Alignment.CenterHorizontally)) { Text("Register") }

                                }
                            }

                        }
                        composable("profile") {
//                            ProfileScreen(
//                                userData = googleAuthUiClient.getSignedInUser(),
//                                onSignOut = {
//                                    lifecycleScope.launch {
//                                        googleAuthUiClient.signOut()
//                                        Toast.makeText(
//                                            applicationContext,
//                                            "Signed out",
//                                            Toast.LENGTH_LONG
//                                        ).show()
//
//                                        navController.popBackStack()
//                                    }
//                                }
//                            )
                            
                            
                            Profile()
                        }
                        composable("log_in") {
                            val state by viewModel.state.collectAsStateWithLifecycle()

                            LaunchedEffect(key1 = Unit) {
                                if (googleAuthUiClient.getSignedInUser() != null) {
                                    navController.navigate("profile")
                                }
                            }
                            val launcher = rememberLauncherForActivityResult(
                                contract = ActivityResultContracts.StartIntentSenderForResult(),
                                onResult = { result ->
                                    if (result.resultCode == ComponentActivity.RESULT_OK) {
                                        lifecycleScope.launch {
                                            val signInResult = googleAuthUiClient.signInWithIntent(
                                                intent = result.data ?: return@launch
                                            )
                                            viewModel.onSignInResult(signInResult)
                                        }
                                    }
                                }
                            )
                            LaunchedEffect(key1 = state.isSignInSuccessful) {
                                if (state.isSignInSuccessful) {
                                    Toast.makeText(
                                        applicationContext,
                                        "Sign in successful",
                                        Toast.LENGTH_LONG
                                    ).show()

                                    navController.navigate("profile")
                                    viewModel.resetState()
                                }
                            }
                            LaunchedEffect(key1 = state.signInError) {
                                state.signInError?.let { error ->
                                    Toast.makeText(
                                        this@MainActivity,
                                        error,
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }

                            Box(Modifier.fillMaxSize()) {
                                Column(Modifier.align(Alignment.Center)) {

                                    LogIn(navController)


                                    Button(onClick = {
                                        lifecycleScope.launch {
                                            val signInIntentSender = googleAuthUiClient.signIn()
                                            launcher.launch(
                                                IntentSenderRequest.Builder(
                                                    signInIntentSender ?: return@launch
                                                ).build()
                                            )
                                        }
                                    },
                                    modifier = Modifier.align(
                                        Alignment.CenterHorizontally)

                                    ) {
                                        Text(text = "Sign in By Google", modifier = Modifier.align(Alignment.CenterVertically))

                                    }
                                }
                                Button(onClick = { navController.navigate("sign_in") }, modifier =  Modifier.align(
                                    Alignment.BottomCenter) ) {
                                    Text(text = "Create Account")
                                }
                            }
                        }
                    }

                }
            }
        }
    }


    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        var currentUser = auth.getCurrentUser()
        //findNavController()
        updateUI(currentUser);
    }

    private fun updateUI(user: FirebaseUser?) {
        Log.d("HEYYYYYYYYYYYYYYYY", user?.email ?: "huita")
    }


    private val googleAuthUiClient by lazy {
        GoogleAuthUiClient(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext)
        )
    }




    private fun notEmpty(email :String ,password:String): Boolean = email.toString().trim().isNotEmpty() &&
            password.toString().trim().isNotEmpty()



    private fun signInUser(navController :NavController,email: String, password: String) {
        if (notEmpty(email ,password)) {

            auth.createUserWithEmailAndPassword(email.trim(), password.trim())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        Toast.makeText(
                            applicationContext ,
                            "created account successfully !",
                            Toast.LENGTH_SHORT
                        ).show()
                        sendEmailVerification(email)
                        navController.navigate("log_in")
                    } else {

                        Toast.makeText(
                            applicationContext,
                            "Faileddd",
                            Toast.LENGTH_SHORT
                        ).show()

                        Log.d("123312213132123", task.result.toString())


                    }
                }
        }
        else{
            Toast.makeText(
                this@MainActivity,
                "You miss something",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    @Composable
    fun LogIn(navController: NavController){

        var email = rememberSaveable { mutableStateOf("") }
        var password = rememberSaveable { mutableStateOf("") }
        Box() {
            Column(Modifier.align(Alignment.Center)) {
                Text(text = "Log In" , fontSize = 24.sp, modifier = Modifier.padding(bottom  =20.dp))
                TextField(
                    value = email.value,
                    onValueChange = { email.value = it },
                    modifier = Modifier.padding(bottom = 10.dp),
                    label  = { Text("Email") }
                )
                TextField(
                    value = password.value,
                    onValueChange = { password.value = it },
                    label  = { Text("Password") })




                Button(onClick = {logInUser(navController,email.value ,password.value)}, modifier =Modifier.align(Alignment.CenterHorizontally)) {

                    Text("Log In",  )

                }
            }
        }
    }

    fun logInUser(navController: NavController, email:String ,password:String){
        
        
        
            if (notEmpty(email,password)) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { signIn ->
                        if (signIn.isSuccessful) {
                            //startActivity(Intent(this@MainActivity, HomeActivity::class.java))
                            Toast.makeText(
                                this@MainActivity,
                                "sined in Successfuly",
                                Toast.LENGTH_SHORT
                            ).show()
                            navController.navigate("profile")


                        } else {
                            Toast.makeText(
                                this@MainActivity,
                                "sined failed",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            } else {
                Toast.makeText(
                    this@MainActivity,
                    "Missed smth",
                    Toast.LENGTH_SHORT
                ).show()
            }


    }


    @Composable
    fun Profile(){
        val googleUser =googleAuthUiClient.getSignedInUser()
        val emailUser = firebaseUser
        Log.d("askjdfhkdsahfklsjadf", googleUser?.username ?: "null")
        Log.d("askjdfhkdsahfklsjadf", emailUser?.email ?: "netu")
        
        
        
        Box(){
            Column(Modifier.align(Alignment.Center)) {


                Text("You signed-in as \n ${emailUser?.email}" , fontSize = 20.sp)
                Button(onClick = { auth.signOut()
                finish()
                }) {
                    Text("Sign Out")
                }
            }

        }
    }


    private fun sendEmailVerification(userEmail: String) {


        firebaseUser?.let {
            it.sendEmailVerification().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        this@MainActivity,
                        "email sent to $userEmail",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    object FirebaseUtils {
        val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
        val firebaseUser: FirebaseUser? = firebaseAuth.currentUser
    }




}