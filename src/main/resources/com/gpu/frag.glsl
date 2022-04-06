/*
 * Copyright (c) 2018, Adam <Adam@sigterm.info>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
#version 330

uniform sampler2DArray textures;
uniform vec2 textureOffsets[64];
uniform float brightness;
uniform float smoothBanding;
uniform vec4 fogColor;
uniform vec4 configGrayColor;
uniform float configGrayAmount;
uniform int colorBlindMode;
uniform float textureLightMode;

in vec4 Color;
noperspective centroid in float fHsl;
flat in int textureId;
in vec2 fUv;
in float fogAmount;
in float grayAmount;

out vec4 FragColor;

#include hsl_to_rgb.glsl
#include colorblind.glsl

float blendSoftLight(float base, float blend) {
	return (blend<0.5)?(2.0*base*blend+base*base*(1.0-2.0*blend)):(sqrt(base)*(2.0*blend-1.0)+2.0*base*(1.0-blend));
}

vec3 blendSoftLight(vec3 base, vec3 blend) {
	return vec3(blendSoftLight(base.r,blend.r),blendSoftLight(base.g,blend.g),blendSoftLight(base.b,blend.b));
}

vec3 blendSoftLight(vec3 base, vec3 blend, float opacity) {
	return (blendSoftLight(base, blend) * opacity + base * (1.0 - opacity));
}

void main() {
  vec4 c;

  if (textureId > 0) {
    int textureIdx = textureId - 1;

    vec4 textureColor = texture(textures, vec3(fUv, float(textureIdx)));
    vec4 textureColorBrightness = pow(textureColor, vec4(brightness, brightness, brightness, 1.0f));

    // textured triangles hsl is a 7 bit lightness 2-126
    float light = fHsl / 127.f;
    vec3 mul = (1.f - textureLightMode) * vec3(light) + textureLightMode * Color.rgb;
    c = textureColorBrightness * vec4(mul, 1.f);
  } else {
    // pick interpolated hsl or rgb depending on smooth banding setting
    vec3 rgb = hslToRgb(int(fHsl)) * smoothBanding + Color.rgb * (1.f - smoothBanding);
    c = vec4(rgb, Color.a);
  }

  if (colorBlindMode > 0) {
    c.rgb = colorblind(colorBlindMode, c.rgb);
  }

  vec3 mixedColor = mix(c.rgb, fogColor.rgb, fogAmount);
  float gray = dot(mixedColor.rgb, vec3(0.299, 0.587, 0.114));
  vec3 grayColor = vec3(gray);
  grayColor = mix(mixedColor.rgb, grayColor.rgb, configGrayAmount);
  grayColor = blendSoftLight(grayColor, configGrayColor.rgb, configGrayColor.a);
  vec3 finalColor = mix(mixedColor.rgb, grayColor.rgb, grayAmount);
  FragColor = vec4(finalColor, c.a);
}
