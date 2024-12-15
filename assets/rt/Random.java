package rt;

import java.nio.ByteBuffer;
import java.security.SecureRandom;

public interface Random {
	final class SeedGenerator implements base.caps.RandomSeed_0 {
		public static final SeedGenerator $self = new SeedGenerator();
		private final static SecureRandom rng = new SecureRandom();
		@Override public Long $hash$mut() {
			long res;
			do { res = rt.Str.wrap(rng.generateSeed(8)).getLong(); } while (res == 0);
			return res;
		}
	}
}
