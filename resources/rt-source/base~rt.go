package main

func baseφrtφConvertBool(b bool) ΦbaseφBool_0 {
	if b {
		return ΦbaseφTrue_0Impl{}
	} else {
		return ΦbaseφFalse_0Impl{}
	}
}

func baseφrtφPow[N int64 | uint64](base N, exp uint64) N {
	var res = base
	if exp == 0 {
		return 1
	}
	for ; exp > 1; exp-- {
		res *= base
	}
	return res
}

func baseφrtφAbs[N int64 | float64](n N) N {
	if n < 0 {
		return -n
	}
	return n
}
