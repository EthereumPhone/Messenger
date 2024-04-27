package org.ethereumhpone.chat.components

import org.intellij.lang.annotations.Language

/* Copyright 2022 Google LLC.
SPDX-License-Identifier: Apache-2.0 */
@Language("AGSL")
const val COLOR_SHADER_SRC =
    """half4 main(float2 fragCoord) {
      return half4(1,0,0,1);
   }"""

val GAUSS_SHADER = """


precision highp float;

uniform sampler2D iChannel0; // Texture unit 0
uniform vec2 iResolution; // Viewport resolution (in pixels)

out vec4 fragColor;

void main() {
    float Pi = 6.28318530718; // Pi*2

    // GAUSSIAN BLUR SETTINGS
    float Directions = 16.0; // BLUR DIRECTIONS (Default 16.0 - More is better but slower)
    float Quality = 3.0; // BLUR QUALITY (Default 4.0 - More is better but slower)
    float Size = 8.0; // BLUR SIZE (Radius)

    vec2 Radius = Size / iResolution;

    // Normalized pixel coordinates (from 0 to 1)
    vec2 uv = gl_FragCoord.xy / iResolution;
    // Pixel colour
    vec4 Color = texture(iChannel0, uv);

    // Blur calculations
    for (float d = 0.0; d < Pi; d += Pi / Directions) {
        for (float i = 1.0 / Quality; i <= 1.0; i += 1.0 / Quality) {
            Color += texture(iChannel0, uv + vec2(cos(d), sin(d)) * Radius * i);
        }
    }

    // Output to screen
    Color /= Quality * Directions - 15.0;
    fragColor = Color;
}


""".trimIndent()
