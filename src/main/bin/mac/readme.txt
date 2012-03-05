The MinGW binutils built on Mac OS X 10.7

Procedure:
  - download sources for binutils version 2.15 from http://ftp.gnu.org/gnu/binutils/
    (Don't use the latest version, at the time of this writing 2.22. The version info
    is missing from the resulting executable wrapper otherwise!)
  - expand into directory BU_DIST
  - in BU_DIST, build with
      CFLAGS="-arch i386" ./configure --target=i686-pc-mingw32 && make -k
  - copy BU_DIST/binutils/windres to directory of this readme
  - copy BU_DIST/ld/ld-new to directory of this readme and rename to 'ld'

See also Launch4j Bug numbers 3385595 and 3439121:
  http://sourceforge.net/tracker/?func=detail&aid=3385595&group_id=95944&atid=613100
  http://sourceforge.net/tracker/?func=detail&aid=3439121&group_id=95944&atid=613100
