#define PI 3.1415926535897932384626433832795f
#define UNIT PI / 1024.0f

#define LOCKED_REGIONS_SIZE 16

uniform int useGray;
uniform int useHardBorder;
uniform int baseX;
uniform int baseY;
uniform int lockedRegions[LOCKED_REGIONS_SIZE];
in float vGrayAmount[];
out float grayAmount;

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

void regionLockerGeomVertex(int i) {
  ivec3 center = (gVertex[0] + gVertex[1] + gVertex[2]) / 3;
  float locked = useGray * isLocked(center.x, center.z);

  grayAmount = useHardBorder * locked + (1 - useHardBorder) * vGrayAmount[i];
}
