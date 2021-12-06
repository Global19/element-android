/*
 * Copyright (c) 2021 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package im.vector.app.features.debug.features

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import im.vector.app.core.extensions.cleanup
import im.vector.app.core.extensions.configureWith
import im.vector.app.databinding.FragmentGenericRecyclerBinding
import im.vector.app.features.DefaultVectorFeatures
import im.vector.app.features.themes.ActivityOtherThemes
import im.vector.app.features.themes.ThemeUtils
import javax.inject.Inject

@AndroidEntryPoint
class DebugFeaturesSettingsActivity : AppCompatActivity() {

    @Inject lateinit var debugFeatures: DebugVectorFeatures
    @Inject lateinit var defaultFeatures: DefaultVectorFeatures

    private lateinit var views: FragmentGenericRecyclerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeUtils.setActivityTheme(this, ActivityOtherThemes.Default)
        views = FragmentGenericRecyclerBinding.inflate(layoutInflater)
        setContentView(views.root)
        val controller = FeaturesController(object : EnumFeatureItem.Listener {

            @Suppress("UNCHECKED_CAST")
            override fun <T : Enum<T>> onOptionSelected(option: Any?, feature: Feature.EnumFeature<T>) {
                debugFeatures.overrideEnum(option as? T, feature.type)
            }
        })
        views.genericRecyclerView.configureWith(controller)
        controller.setData(createState())
    }

    private fun createState(): FeaturesState {
        return FeaturesState(listOf(
                createEnumFeature(
                        label = "Login version",
                        selection = debugFeatures.loginVersion(),
                        default = defaultFeatures.loginVersion()
                )
        ))
    }

    private inline fun <reified T : Enum<T>> createEnumFeature(label: String, selection: T, default: T): Feature {
        return Feature.EnumFeature(
                label = label,
                selection = selection.takeIf { debugFeatures.hasEnumOverride(T::class) },
                default = default,
                options = enumValues<T>().toList(),
                type = T::class
        )
    }

    override fun onDestroy() {
        views.genericRecyclerView.cleanup()
        super.onDestroy()
    }
}
