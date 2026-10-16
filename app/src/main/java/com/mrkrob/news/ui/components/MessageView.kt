package com.mrkrob.news.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mrkrob.news.R
import com.mrkrob.news.domain.model.NewsException

@Composable
fun MessageView(
    @DrawableRes illustration: Int,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(illustration),
                contentDescription = null,
                modifier = Modifier.size(112.dp),
            )
            Spacer(Modifier.height(24.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            if (action != null) {
                Spacer(Modifier.height(24.dp))
                action()
            }
        }
    }
}

@Composable
fun ErrorMessage(
    error: Throwable,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (error) {
        is NewsException.NoConnection -> MessageView(
            illustration = R.drawable.illustration_offline,
            title = stringResource(R.string.error_offline_title),
            message = stringResource(R.string.error_offline_message),
            modifier = modifier,
            action = { RetryButton(onRetry) },
        )
        is NewsException.InvalidApiKey -> MessageView(
            illustration = R.drawable.illustration_key,
            title = stringResource(R.string.error_api_key_title),
            message = stringResource(R.string.error_api_key_message),
            modifier = modifier,
        )
        is NewsException.RateLimited -> MessageView(
            illustration = R.drawable.illustration_hourglass,
            title = stringResource(R.string.error_rate_limited_title),
            message = stringResource(R.string.error_rate_limited_message),
            modifier = modifier,
            action = { RetryButton(onRetry) },
        )
        else -> MessageView(
            illustration = R.drawable.illustration_warning,
            title = stringResource(R.string.error_generic_title),
            message = error.message ?: stringResource(R.string.error_generic_message),
            modifier = modifier,
            action = { RetryButton(onRetry) },
        )
    }
}

@Composable
private fun RetryButton(onClick: () -> Unit) {
    FilledTonalButton(onClick = onClick) {
        Icon(
            painter = painterResource(R.drawable.ic_refresh),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(stringResource(R.string.retry))
    }
}
