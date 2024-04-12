package utils;

import rt.ThrowingConsumer;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

public interface DeleteOnExit {
  static void of(Path dir) {
    try(var tree = IoErr.of(()->Files.walk(dir))) {
      tree.map(Path::toFile).forEach(ThrowingConsumer.of(File::deleteOnExit));
    }
  }
}
