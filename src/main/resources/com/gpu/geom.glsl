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

#define PI 3.1415926535897932384626433832795f
#define UNIT PI / 1024.0f

#define LOCKED_REGIONS_SIZE 16

layout(triangles) in;
layout(triangle_strip, max_vertices = 3) out;

layout(std140) uniform uniforms {
  int cameraYaw;
  int cameraPitch;
  int centerX;
  int centerY;
  int zoom;
  int cameraX;
  int cameraY;
  int cameraZ;
  ivec2 sinCosTable[2048];
};

uniform mat4 projectionMatrix;
uniform int useGray;
uniform int useHardBorder;
uniform int baseX;
uniform int baseY;
uniform int lockedRegions[LOCKED_REGIONS_SIZE];

in float vGrayAmount[];
in ivec3 vPosition[];
in vec4 vColor[];
in float vHsl[];
in int vTextureId[];
in vec2 vUv[];
in float vFogAmount[];

out vec4 Color;
noperspective centroid out float fHsl;
flat out int textureId;
out vec2 fUv;
out float fogAmount;
out float grayAmount;

#include to_screen.glsl

int toRegionId(int x, int y) {
  return (x >> 13 << 8) + (y >> 13);
}

float b_convert(float n) {
  return clamp(abs(n), 0.0, 1.0);
}

float isLocked(int x, int y) {
  x = x + baseX;
  y = y + baseY;
  float result = 1.0;
  for (int i = 0; i < LOCKED_REGIONS_SIZE; ++i) {
    int region = toRegionId(x, y);
    result = result * (lockedRegions[i] - region);
  }
  return b_convert(result);
}

void main() {
  ivec3 center = (vPosition[0] + vPosition[1] + vPosition[2])/3;
  float locked = useGray * isLocked(center.x, center.z);

  for (int i = 0; i < 3; i++) {
    Color = vColor[i];
    fHsl = vHsl[i];
    fUv = vUv[i];
    textureId = vTextureId[i];
    fogAmount = vFogAmount[i];
    grayAmount = useHardBorder * locked + (1 - useHardBorder) * vGrayAmount[i];
    gl_Position  = projectionMatrix * vec4(vPosition[i], 1.f);
    EmitVertex();
  }

  EndPrimitive();
}
