package utils;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

public final class ResolveResource {
  static private final Path root;
  static private FileSystem virtualFs;
  static{
    var url= ResolveResource.class.getResource("/base");
    if(url==null) {
      String workingDir = System.getProperty("user.dir");
      root= Path.of(workingDir).resolve("resources");
      assert Files.exists(root):root;
    }
    else {
      URI uri; try { uri= url.toURI();}
      catch (URISyntaxException e) { throw Bug.of(e); }
      root=Path.of(uri).getParent();
      var inJar= uri.getScheme().equals("jar")
        || uri.getScheme().equals("resource");
      if (inJar) { IoErr.of(()->{
          virtualFs = FileSystems.newFileSystem(uri, Map.of());
          });}
    }
  }
  static public Path of(String relativePath) {
    assert relativePath.startsWith("/");
    URI absolutePath= root.resolve(relativePath.substring(1)).toUri();
    if (virtualFs != null) {
      return virtualFs.getPath(relativePath);
    }
    return Path.of(absolutePath);
  }

  static public String getAndRead(String path) {
    return read(of(path));
  }

  static public String read(Path path) {
    return IoErr.of(()->Files.readString(path, StandardCharsets.UTF_8));
  }
  static public Path freshTmpPath(){
    return Paths.get(
      System.getProperty("java.io.tmpdir"),
      "fearOut"+UUID.randomUUID());
  }
}
