
#include <time.h>

unsigned long long currentTimeNano() {
  struct timespec t;
  clock_gettime(CLOCK_MONOTONIC, &t);
  return t.tv_sec*1000000000 + t.tv_nsec;
}

unsigned long long currentTimeMillis() {
  struct timespec t;
  clock_gettime(CLOCK_MONOTONIC, &t);
  return t.tv_sec*1000 + t.tv_nsec/1000000;
}


