package com.jerboa.ui.components.post.edit

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.jerboa.api.uploadPictrsImage
import com.jerboa.db.AccountViewModel
import com.jerboa.getCurrentAccount
import com.jerboa.ui.components.community.CommunityViewModel
import com.jerboa.ui.components.home.HomeViewModel
import com.jerboa.ui.components.person.PersonProfileViewModel
import com.jerboa.ui.components.post.PostViewModel
import kotlinx.coroutines.launch

@Composable
fun PostEditActivity(
    accountViewModel: AccountViewModel,
    postEditViewModel: PostEditViewModel,
    navController: NavController,
    postViewModel: PostViewModel,
    personProfileViewModel: PersonProfileViewModel,
    communityViewModel: CommunityViewModel,
    homeViewModel: HomeViewModel,
) {

    Log.d("jerboa", "got to post edit activity")

    val ctx = LocalContext.current
    val account = getCurrentAccount(accountViewModel = accountViewModel)
    val scope = rememberCoroutineScope()

    val pv = postEditViewModel.postView.value
    var name by rememberSaveable { mutableStateOf(pv?.post?.name.orEmpty()) }
    var url by rememberSaveable { mutableStateOf(pv?.post?.url.orEmpty()) }
    var body by rememberSaveable { mutableStateOf(pv?.post?.body.orEmpty()) }
    var formValid by rememberSaveable { mutableStateOf(true) }

    Surface(color = MaterialTheme.colors.background) {
        Scaffold(
            topBar = {
                Column {
                    EditPostHeader(
                        navController = navController,
                        formValid = formValid,
                        onEditPostClick = {
                            account?.also { acct ->
                                // Clean up that data
                                val nameOut = name.trim()
                                val bodyOut = body.trim().ifEmpty { null }
                                val urlOut = url.trim().ifEmpty { null }

                                postEditViewModel.editPost(
                                    account = acct,
                                    ctx = ctx,
                                    body = bodyOut,
                                    url = urlOut,
                                    name = nameOut,
                                    navController = navController,
                                    postViewModel = postViewModel,
                                    personProfileViewModel = personProfileViewModel,
                                    communityViewModel = communityViewModel,
                                    homeViewModel = homeViewModel,
                                )
                            }
                        }
                    )
                    if (postEditViewModel.loading.value) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }
            },
            content = {
                EditPostBody(
                    name = name,
                    onNameChange = { name = it },
                    body = body,
                    onBodyChange = { body = it },
                    url = url,
                    onUrlChange = { url = it },
                    formValid = { formValid = it },
                    onPickedImage = { uri ->
                        scope.launch {
                            account?.also { acct ->
                                url = uploadPictrsImage(acct, uri, ctx)
                            }
                        }
                    }
                )
            }
        )
    }
}
