package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CorporateFare
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class CategoryMeta(
    val titleUrdu: String,
    val titleEn: String,
    val description: String,
    val icon: ImageVector,
    val dbCategoryKey: String
)

@Composable
fun CategoriesScreen(
    onSelectCategory: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryList = listOf(
        CategoryMeta(
            titleUrdu = "تعلیم و امتحانات",
            titleEn = "Academic & Education",
            description = "اسکول، کالج، یونیورسٹی اور امتحانات سے متعلق ضروری اصطلاحات",
            icon = Icons.Default.School,
            dbCategoryKey = "تعلیم (Academic)"
        ),
        CategoryMeta(
            titleUrdu = "سائنس و ٹیکنالوجی",
            titleEn = "Science & Technology",
            description = "کمپیوٹر سائنس، مصنوعی ذہانت، طبیعیات اور حیاتیات کی جدید اصطلاحات",
            icon = Icons.Default.Memory,
            dbCategoryKey = "سائنس و ٹیکنالوجی (Science & Tech)"
        ),
        CategoryMeta(
            titleUrdu = "طب و صحت",
            titleEn = "Medical & Healthcare",
            description = "ڈاکٹرز، نرسز، میڈیکل طلبہ اور صحت و علاج سے متعلق بنیادی الفاظ",
            icon = Icons.Default.LocalHospital,
            dbCategoryKey = "طب و صحت (Medical)"
        ),
        CategoryMeta(
            titleUrdu = "قانون و انصاف",
            titleEn = "Law & Judiciary",
            description = "وکلاء، عدلیہ، آئین اور قانونی مقدمات کے مخصوص الفاظ اور اصطلاحات",
            icon = Icons.Default.AccountBalance,
            dbCategoryKey = "قانون و انصاف (Law & Justice)"
        ),
        CategoryMeta(
            titleUrdu = "تجارت و معاشیات",
            titleEn = "Business & Economics",
            description = "بینکاری، سرمایہ کاری، تجارت اور معاشی نظام کے الفاظ",
            icon = Icons.Default.BusinessCenter,
            dbCategoryKey = "تجارت و معاشیات (Business)"
        ),
        CategoryMeta(
            titleUrdu = "دفتری و انتظامی",
            titleEn = "Office & Administration",
            description = "ملازمت، انتظامی فیصلے اور پیشہ ورانہ خط و کتابت کی اصطلاحات",
            icon = Icons.Default.CorporateFare,
            dbCategoryKey = "دفتری و انتظامی (Administration)"
        ),
        CategoryMeta(
            titleUrdu = "روزمرہ بول چال",
            titleEn = "Daily Life & Conversation",
            description = "معاشرتی گفتگو، اخلاق، احساسات اور روزمرہ تعاملات کے الفاظ",
            icon = Icons.Default.ChatBubbleOutline,
            dbCategoryKey = "روزمرہ بول چال (Daily Life)"
        ),
        CategoryMeta(
            titleUrdu = "ادب و فنون",
            titleEn = "Literature & Arts",
            description = "شاعری، نثر، علمی کتب اور ادبی فصاحت و بلاغت کے الفاظ",
            icon = Icons.AutoMirrored.Filled.MenuBook,
            dbCategoryKey = "ادب و شاعری (Literature)"
        )
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("categories_screen_list"),
        contentPadding = PaddingValues(16.dp, bottom = 96.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(bottom = 12.dp)) {
                Text(
                    text = "شعبہ ہائے زندگی کے مطابق ذخیرہ الفاظ",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "طالب علموں، ڈاکٹروں، وکلاء، تاجروں اور پیشہ ور افراد کے لیے مخصوص زمرہ جات",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }

        items(categoryList) { item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable { onSelectCategory(item.dbCategoryKey) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(52.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = item.titleUrdu,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Text(
                                text = item.titleEn,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = item.description,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "دیکھیں",
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}
