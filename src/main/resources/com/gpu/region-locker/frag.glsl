uniform vec4 configGrayColor;
uniform float configGrayAmount;
in float grayAmount;

float blendSoftLight(float base, float blend) {
  return (blend < 0.5) ? (2.0 * base * blend + base * base * (1.0 - 2.0 * blend)) : (sqrt(base) * (2.0 * blend - 1.0) + 2.0 * base * (1.0 - blend));
}

vec3 blendSoftLight(vec3 base, vec3 blend) {
  return vec3(blendSoftLight(base.r, blend.r), blendSoftLight(base.g, blend.g), blendSoftLight(base.b, blend.b));
}

vec3 blendSoftLight(vec3 base, vec3 blend, float opacity) {
  return (blendSoftLight(base, blend) * opacity + base * (1.0 - opacity));
}

vec4 regionLockerShading(vec4 color) {
  float gray = dot(color.rgb, vec3(0.299, 0.587, 0.114));
  vec3 grayColor = vec3(gray);
  grayColor = mix(color.rgb, grayColor.rgb, configGrayAmount);
  grayColor = blendSoftLight(grayColor, configGrayColor.rgb, configGrayColor.a);
  vec3 finalColor = mix(color.rgb, grayColor.rgb, grayAmount);
  return vec4(finalColor, color.a);
}
