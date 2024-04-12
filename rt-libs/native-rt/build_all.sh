#!/bin/sh
CROSS_CONTAINER_OPTS="--platform linux/amd64" cross build --release --target x86_64-unknown-linux-gnu
cp target/x86_64-unknown-linux-gnu/release/libnative_rt.so ../../resources/rt/libnative/amd64-libnative_rt.so

cross build --release --target aarch64-unknown-linux-gnu
cp target/aarch64-unknown-linux-gnu/release/libnative_rt.so ../../resources/rt/libnative/arm64-libnative_rt.so

CROSS_CONTAINER_OPTS="--platform linux/amd64" cross build --release --target x86_64-pc-windows-gnu
cp target/x86_64-pc-windows-gnu/release/native_rt.dll ../../resources/rt/libnative/amd64-native_rt.dll

cross build --release --target aarch64-apple-darwin
cp target/aarch64-apple-darwin/release/libnative_rt.dylib ../../resources/rt/libnative/arm64-libnative_rt.dylib

cross build --release --target x86_64-apple-darwin
cp target/x86_64-apple-darwin/release/libnative_rt.dylib ../../resources/rt/libnative/amd64-libnative_rt.dylib
