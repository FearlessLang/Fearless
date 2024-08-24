#[cfg(target_arch = "x86_64")]
#[inline(always)]
pub(crate) fn now() -> u64 {
    // Safety: We are on an x86_64 CPU
    unsafe { std::arch::x86_64::_rdtsc() }
}

#[cfg(not(target_arch = "x86_64"))]
#[inline(always)]
pub(crate) fn now() -> u64 {
    coarsetime::Instant::now().as_ticks()
}
