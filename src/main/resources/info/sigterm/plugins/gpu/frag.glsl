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

//#define FRAG_UVS
//#define ZBUF_DEBUG

#include colorblind_mode
#include gpu_api_scene_config

uniform sampler2DArray textures;
uniform float brightness;
uniform float smoothBanding;
uniform vec4 fogColor;
uniform float textureLightMode;

in vec4 fColor;
noperspective centroid in float fHsl;
flat in int fTextureId;
in vec2 fUv;
in float fFogAmount;
#ifdef GPU_API_SCENE_EFFECT
in float fCameraDistance;
flat in int fSurfaceType;
in vec3 fWorldPosition;
in vec3 fViewDirection;
#endif
#ifdef ZBUF_DEBUG
in float fDepth;
#endif

out vec4 FragColor;

#ifdef GPU_API_SCENE_EFFECT
struct GpuSceneInput {
  vec3 worldPosition;
  float cameraDistance;
  vec3 viewDirection;
  vec3 surfaceNormal;
  float fogAmount;
  int surfaceType;
};

struct GpuSceneOutput {
  vec3 surfaceColor;
  vec3 fogColor;
};

#include gpu_api_scene_effect
#endif

#include "hsl_to_rgb.glsl"

#if COLORBLIND_MODE > 0
#include "colorblind.glsl"
#endif

#ifdef ZBUF_DEBUG
float linear_depth(float depth) {
  // depth is computed as 100/z, solve for z
  float z = 100 / depth;
  return 1 - z / 10000;  // we don't have a far plane, but the client uses 10000
}
#endif

#ifdef GPU_API_SCENE_EFFECT
vec3 gpuApiSurfaceNormal(vec3 worldPosition, vec3 viewDirection) {
  vec3 normal = cross(dFdx(worldPosition), dFdy(worldPosition));
  float lengthSquared = dot(normal, normal);
  if (lengthSquared < 0.000001) {
    return viewDirection;
  }
  normal *= inversesqrt(lengthSquared);
  return dot(normal, viewDirection) < 0.0 ? -normal : normal;
}
#endif

void main() {
  vec4 c;

  if (fTextureId > 0) {
    int textureIdx = fTextureId - 1;

    vec4 textureColor = texture(textures, vec3(fUv, float(textureIdx)));
    vec4 textureColor0 = textureLod(textures, vec3(fUv, float(textureIdx)), 0.f);

    if (textureColor0.a < 1.f)
      discard;

    textureColor = vec4(textureColor.rgb, 1.f);

    textureColor = pow(textureColor, vec4(brightness, brightness, brightness, 1.f));

    // textured triangles hsl is a 7 bit lightness 2-126
    float light = fHsl / 127.f;
    vec3 mul = (1.f - textureLightMode) * vec3(light) + textureLightMode * fColor.rgb;
    c = textureColor * vec4(mul, fColor.a);
  } else {
    // pick interpolated hsl or rgb depending on smooth banding setting
    vec3 hsl = vec3(int(fHsl) >> 10 & 63, int(fHsl) >> 7 & 7, int(fHsl) & 127);
    vec3 rgb = mix(fColor.rgb, hslToRgb(hsl), smoothBanding);
    c = vec4(rgb, fColor.a);
  }

#if COLORBLIND_MODE > 0
  c.rgb = colorblind(c.rgb);
#endif

#ifdef GPU_API_SCENE_EFFECT
  vec3 viewDirection = normalize(fViewDirection);
  GpuSceneInput gpuInput = GpuSceneInput(
      fWorldPosition,
      fCameraDistance,
      viewDirection,
      gpuApiSurfaceNormal(fWorldPosition, viewDirection),
      fFogAmount,
      fSurfaceType);
  GpuSceneOutput gpuOutput = GpuSceneOutput(c.rgb, fogColor.rgb);
  applySceneEffect(gpuInput, gpuOutput);

  vec3 mixedColor = mix(gpuOutput.surfaceColor, gpuOutput.fogColor, fFogAmount);
#else
  vec3 mixedColor = mix(c.rgb, fogColor.rgb, fFogAmount);
#endif
  FragColor = vec4(mixedColor, c.a);

#ifdef FRAG_UVS
  if (fTextureId > 0) {
    FragColor = vec4(fUv.x, 0, fUv.y, 1);
  }
#endif

#ifdef ZBUF_DEBUG
  float dc = linear_depth(fDepth);
  if (dc > 1.0) {
    FragColor = vec4(1, 0, 0, 1);
  } else if (dc < -1.0) {
    FragColor = vec4(0, 0, 1, 1);
  } else if (dc < 0.0) {
    FragColor = vec4(0, 1, 0, 1);
  } else {
    FragColor = vec4(dc, dc, dc, 1);
  }
#endif
}
