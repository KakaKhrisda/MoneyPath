package com.example.moneypath.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DarkGreen = Color(0xFF0B6B4B)
private val BrandGreen = Color(0xFF12A36B)
private val InkGreen = Color(0xFF0B2E24)
private val MutedText = Color(0xFF4B5F57)

@Composable
fun OnboardingScreen(
    onGetStarted: () -> Unit,
    onLogin: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Color(0xFFEAF6F0), Color(0xFFF6FBF8)))
            )
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        // Top row: logo + name, Log in link
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                MoneyPathLogo(logoSize = 44.dp)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "MoneyPath",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = InkGreen,
                        fontSize = 28.sp
                    )
                )
            }
            Text(
                text = "Log in",
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onLogin() }
                    .padding(8.dp),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = BrandGreen
                )
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Illustration card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3FAF6)),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            LandingIllustration(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Headline
        Text(
            text = buildAnnotatedString {
                append("Plan your money.\nBuild your ")
                withStyle(SpanStyle(color = BrandGreen)) { append("tomorrow.") }
            },
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                color = InkGreen,
                fontSize = 34.sp,
                lineHeight = 40.sp
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Set budgets, track expenses, and stay on course with your financial goals.",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = MutedText,
                fontSize = 16.sp,
                lineHeight = 24.sp
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Feature chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FeatureChip(Icons.Default.AccountBalanceWallet, "Budgets", Modifier.weight(1f))
            FeatureChip(Icons.Default.Receipt, "Expenses", Modifier.weight(1f))
            FeatureChip(Icons.Default.TrackChanges, "Goals", Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Get Started button
        Button(
            onClick = onGetStarted,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DarkGreen)
        ) {
            Text(
                text = "Get Started",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 18.sp
                )
            )
            Spacer(modifier = Modifier.width(10.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = Color.White
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Already have an account
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Already have an account? ",
                style = MaterialTheme.typography.bodyMedium.copy(color = MutedText)
            )
            Text(
                text = "Log in",
                modifier = Modifier.clickable { onLogin() },
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = BrandGreen,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Page dots
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(DarkGreen))
            Spacer(modifier = Modifier.width(10.dp))
            Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(Color(0xFFCFE3DA)))
            Spacer(modifier = Modifier.width(10.dp))
            Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(Color(0xFFCFE3DA)))
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Works offline
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(BrandGreen))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Works offline",
                style = MaterialTheme.typography.bodyMedium.copy(color = MutedText)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun FeatureChip(icon: ImageVector, label: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFE1F0E8))
            .padding(horizontal = 10.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = DarkGreen,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            maxLines = 1,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = InkGreen,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

// Wallet, cash, coins and a growth arrow, drawn in code
@Composable
private fun LandingIllustration(modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier = modifier) {
        val w = maxWidth
        val h = maxHeight

        Canvas(modifier = Modifier.fillMaxSize()) {
            val cw = size.width
            val ch = size.height

            // Soft hill shadow
            drawOval(
                color = Color(0xFFDDF0E6),
                topLeft = Offset(cw * 0.12f, ch * 0.70f),
                size = Size(cw * 0.78f, ch * 0.22f)
            )

            // Growth swoosh and arrow head
            val swoosh = Path().apply {
                moveTo(cw * 0.22f, ch * 0.80f)
                cubicTo(cw * 0.36f, ch * 0.52f, cw * 0.54f, ch * 0.66f, cw * 0.70f, ch * 0.36f)
            }
            drawPath(
                path = swoosh,
                color = Color(0xFF8FD6B3).copy(alpha = 0.75f),
                style = Stroke(width = 26f, cap = StrokeCap.Round)
            )
            val head = Path().apply {
                moveTo(cw * 0.80f, ch * 0.20f)
                lineTo(cw * 0.62f, ch * 0.30f)
                lineTo(cw * 0.76f, ch * 0.44f)
                close()
            }
            drawPath(path = head, color = Color(0xFF5CC495))

            // Dotted arc
            val arc = Path().apply {
                moveTo(cw * 0.17f, ch * 0.66f)
                cubicTo(cw * 0.22f, ch * 0.10f, cw * 0.70f, ch * 0.02f, cw * 0.84f, ch * 0.52f)
            }
            drawPath(
                path = arc,
                color = Color(0xFF3FAF82).copy(alpha = 0.7f),
                style = Stroke(
                    width = 4f,
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 14f))
                )
            )

            // Wallet body
            drawRoundRect(
                color = Color(0xFF0B5D42),
                topLeft = Offset(cw * 0.48f, ch * 0.40f),
                size = Size(cw * 0.32f, ch * 0.40f),
                cornerRadius = CornerRadius(26f, 26f)
            )
            // Wallet flap strip
            drawRoundRect(
                color = Color(0xFF0F7A55),
                topLeft = Offset(cw * 0.48f, ch * 0.40f),
                size = Size(cw * 0.32f, ch * 0.09f),
                cornerRadius = CornerRadius(26f, 26f)
            )
            // Wallet clasp
            drawRoundRect(
                color = Color(0xFF0F7A55),
                topLeft = Offset(cw * 0.72f, ch * 0.54f),
                size = Size(cw * 0.12f, ch * 0.13f),
                cornerRadius = CornerRadius(22f, 22f)
            )
            drawCircle(
                color = Color(0xFFCFE8DA),
                radius = 11f,
                center = Offset(cw * 0.765f, ch * 0.605f)
            )

            // Banknote
            drawRoundRect(
                color = Color(0xFF58C08E),
                topLeft = Offset(cw * 0.26f, ch * 0.50f),
                size = Size(cw * 0.27f, ch * 0.30f),
                cornerRadius = CornerRadius(14f, 14f)
            )
            drawRoundRect(
                color = Color(0xFF9BDDBB),
                topLeft = Offset(cw * 0.285f, ch * 0.54f),
                size = Size(cw * 0.22f, ch * 0.22f),
                cornerRadius = CornerRadius(10f, 10f)
            )
            drawCircle(
                color = Color(0xFF2EA36F),
                radius = cw * 0.045f,
                center = Offset(cw * 0.395f, ch * 0.65f)
            )

            // Coin stack
            for (i in 0..3) {
                drawOval(
                    color = Color(0xFF3FA574),
                    topLeft = Offset(cw * 0.22f, ch * (0.77f - i * 0.03f)),
                    size = Size(cw * 0.13f, ch * 0.07f)
                )
                drawOval(
                    color = Color(0xFF5FC896),
                    topLeft = Offset(cw * 0.22f, ch * (0.755f - i * 0.03f)),
                    size = Size(cw * 0.13f, ch * 0.07f)
                )
            }
            // Single coin
            drawCircle(
                color = Color(0xFF9FC870),
                radius = cw * 0.045f,
                center = Offset(cw * 0.38f, ch * 0.80f)
            )
            drawCircle(
                color = Color(0xFFC3DC8E),
                radius = cw * 0.03f,
                center = Offset(cw * 0.38f, ch * 0.80f)
            )
        }

        // Floating icon tiles
        FloatingTile(Icons.Default.Receipt, Modifier.offset(x = w * 0.5f - 30.dp, y = h * 0.08f))
        FloatingTile(Icons.Default.AccountBalanceWallet, Modifier.offset(x = w * 0.17f, y = h * 0.28f))
        FloatingTile(Icons.Default.TrackChanges, Modifier.offset(x = w * 0.80f - 30.dp, y = h * 0.40f))
    }
}

@Composable
private fun FloatingTile(icon: ImageVector, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(58.dp)
            .shadow(6.dp, RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = DarkGreen,
            modifier = Modifier.size(30.dp)
        )
    }
}