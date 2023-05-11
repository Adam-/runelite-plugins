#define LOCKED_REGIONS_SIZE 16
uniform int useGray;
uniform int baseX;
uniform int baseY;
uniform int lockedRegions[LOCKED_REGIONS_SIZE];
out float vGrayAmount;

const ivec2 regionOffsets[5] = ivec2[](ivec2(0, 0), ivec2(-1, -1), ivec2(-1, 1), ivec2(1, -1), ivec2(1, 1));

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
    for (int j = 0; j < regionOffsets.length(); ++j) {
      ivec2 off = regionOffsets[j];
      int region = toRegionId(x + off.x, y + off.y);
      result = result * (lockedRegions[i] - region);
    }
  }
  return b_convert(result);
}

void regionLockerVert(ivec3 vertex) {
  vGrayAmount = useGray * isLocked(int(vertex.x), int(vertex.z));
}
