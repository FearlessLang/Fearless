use coarsetime::Instant;

#[cfg(target_arch = "x86_64")]
#[inline(always)]
fn now() -> Instant {
    // Safety: We are on an x86_64 CPU
    Instant(unsafe { std::arch::x86_64::_rdtsc() })
}

#[cfg(not(target_arch = "x86_64"))]
#[inline(always)]
fn now() -> Instant {
    Instant::now()
}
