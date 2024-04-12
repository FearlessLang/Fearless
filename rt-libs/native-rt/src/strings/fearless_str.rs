use std::marker::PhantomData;
use std::slice;
use std::str::Utf8Error;
use jni::JNIEnv;

use jni::objects::{AutoElements, JByteArray, ReleaseMode};
use jni::sys::jbyte;

#[repr(transparent)]
pub struct FearlessStr<'a, 'local, 'array_local, 'array> {
    array_ref: AutoElements<'local, 'array_local, 'array, jbyte>,
    _keep_alive: PhantomData<&'a ()>,
}
impl<'a, 'local, 'array_local, 'array> FearlessStr<'a, 'local, 'array_local, 'array> {
    pub fn new(env: &mut JNIEnv<'local>, utf8_str: &'array JByteArray<'array_local>) -> FearlessStr<'a, 'local, 'array_local, 'array> {
        // Safety: Lifetimes bind the data here. Making this critical would be safe too,
        // but we don't need to do that unless this is becoming a perf issue (making it critical
        // has stalling risks on other threads because it locks the allocator)
        let array_ref = unsafe { env.get_array_elements(utf8_str, ReleaseMode::NoCopyBack).unwrap() };
        Self { array_ref, _keep_alive: PhantomData }
    }
    pub fn validate(&self) -> Option<Utf8Error> {
        let raw_str = self.as_bytes();
        std::str::from_utf8(raw_str).err()
    }
    /// # Safety
    /// You have called `self.validate()` on this string, and it returned `None`.
    pub unsafe fn as_str(&self) -> &str {
        let raw_str = self.as_bytes();
        std::str::from_utf8_unchecked(raw_str)
    }
    pub fn as_bytes(&'a self) -> &'a [u8] {
        // Safety:
        // - The JNI guarantees that there is data here with the provided length.
        // - i8 and u8 are safely interchangeable for the bytes here
        // - The data is only guaranteed to live while the FearlessStr struct does, so we
        // need to bind it to the same lifetime.
        unsafe {
            let java_bytes: &[i8] = slice::from_raw_parts(self.array_ref.as_ptr(), self.array_ref.len());
            std::mem::transmute(java_bytes)
        }
    }
}
