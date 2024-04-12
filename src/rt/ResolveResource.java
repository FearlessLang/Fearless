package rt;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Collectors;

public final class ResolveResource {
  static private final Path root;
  static private FileSystem virtualFs;
  static{
    var url= ResolveResource.class.getResource("/base");
    if(url==null) {
      String workingDir = System.getProperty("user.dir");
      root=Path.of(workingDir).resolve("resources");
      assert Files.exists(root):root;
    } else {
      URI uri; try { uri= url.toURI();}
      catch (URISyntaxException e) { throw new RuntimeException(e); }

      if (uri.getScheme().equals("jar") || uri.getScheme().equals("resource")) {
        try {
          virtualFs = FileSystems.newFileSystem(uri, Map.of());
          root = virtualFs.getPath("/");
        } catch (IOException e) {
          throw new UncheckedIOException(e);
        }
      } else {
        root=Path.of(uri).getParent();
      }
    }
  }
  static public Path of(String relativePath) {
    assert relativePath.startsWith("/");
    if (virtualFs != null) {
      return virtualFs.getPath(relativePath);
    }
    URI absolutePath= root.resolve(relativePath.substring(1)).toUri();
    return Path.of(absolutePath);
  }

  static public String getAndRead(String path) {
    return read(of(path));
  }

  static public String read(Path path) {
    try(var br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
      return br.lines().collect(Collectors.joining("\n"));
    } catch (IOException err) {
      throw new UncheckedIOException(err);
    }
  }
}
