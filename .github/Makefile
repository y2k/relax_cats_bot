# GENERATED FILE - DO NOT EDIT

all: _p__1 _p__2
SRC_DIR_p__1 := src
OUT_DIR_p__1 := .github/bin/src
EXT_IN_p__1 := clj
EXT_OUT_p__1 := js
SRC_FILES_p__1 := $(shell find -L $(SRC_DIR_p__1) -type f -name '*.$(EXT_IN_p__1)')
OUT_FILES_p__1 := $(patsubst $(SRC_DIR_p__1)/%.$(EXT_IN_p__1),$(OUT_DIR_p__1)/%.$(EXT_OUT_p__1),$(SRC_FILES_p__1))

_p__1: $(OUT_FILES_p__1)

$(OUT_DIR_p__1)/%.$(EXT_OUT_p__1): $(SRC_DIR_p__1)/%.$(EXT_IN_p__1)
	@ mkdir -p $(dir $@)
	cy2k -log false -target js -src $< -root src -namespace nil > $@

SRC_DIR_p__2 := test
OUT_DIR_p__2 := .github/bin/test
EXT_IN_p__2 := clj
EXT_OUT_p__2 := js
SRC_FILES_p__2 := $(shell find -L $(SRC_DIR_p__2) -type f -name '*.$(EXT_IN_p__2)')
OUT_FILES_p__2 := $(patsubst $(SRC_DIR_p__2)/%.$(EXT_IN_p__2),$(OUT_DIR_p__2)/%.$(EXT_OUT_p__2),$(SRC_FILES_p__2))

_p__2: $(OUT_FILES_p__2)

$(OUT_DIR_p__2)/%.$(EXT_OUT_p__2): $(SRC_DIR_p__2)/%.$(EXT_IN_p__2)
	@ mkdir -p $(dir $@)
	cy2k -log false -target js -src $< -root test -namespace nil > $@


