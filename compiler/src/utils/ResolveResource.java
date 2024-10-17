package utils;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public record ResolveResource(Path assetRoot, Path artefactRoot, Optional<Path> testsRoot, FileSystem virtualFs) {
  static private final ResolveResource instance= 
    ResolveResource.infer();
    //ResolveResource.of(Paths.get("C:/")
    //  .resolve("Users/sonta/Documents/GitHub/Fearless"));
  //Infer works if we put the two data folders in the class path

  public ResolveResource{
    assert Files.exists(assetRoot):assetRoot;
    assert Files.exists(artefactRoot):artefactRoot;
  }

  static ResolveResource infer() {
    var url= ResolveResource.class.getResource("/base");
    var testResourceUrl= Optional.ofNullable(ResolveResource.class.getResource("/.compiler-tests"));
    if(url==null) {
      // We're running with a working dir of the Fearless Compiler project, likely in something like Eclipse.
      var root = Path.of("").toAbsolutePath().getParent();
      return new ResolveResource(
        root.resolve("assets"),
        root.resolve("artefacts"),
        Optional.of(root.resolve("compiler-tests")),
        null
      );
    }

    URI resourcesUri; try { resourcesUri= url.toURI();}
    catch (URISyntaxException e) { throw Bug.of(e); }
    Optional<URI> testResourceUri= testResourceUrl.map(testResourceUrl_ -> {
      try {
        return testResourceUrl_.toURI();
      } catch (URISyntaxException e) {
        throw Bug.of(e);
      }
    });

    var inBundle = resourcesUri.getScheme().equals("jar") || resourcesUri.getScheme().equals("resource");
    if (!inBundle){
      // We're running in an IDE/build tool that uses an output/build dir as the working dir. Likely IntelliJ or Maven.
      var resourcesRoot= Path.of(resourcesUri).getParent();
      var testResourcesRoot= testResourceUri.map(uri->Path.of(uri).getParent());
      return new ResolveResource(resourcesRoot, resourcesRoot, testResourcesRoot, null);
    }
    // We're in a JAR or AOT Application Image.
    return IoErr.of(()->{
      var virtualFs= FileSystems.newFileSystem(resourcesUri, Map.of());
      var both= virtualFs.getPath("/");
      return new ResolveResource(both, both, null, virtualFs);
    });
  }
  static public Path asset(String relativePath) {
    return of(instance.assetRoot, relativePath);
  }
  static public Path artefact(String relativePath) {
    return of(instance.artefactRoot, relativePath);
  }
  static public Path test(String relativePath) {
    return of(instance.testsRoot.orElseThrow(), relativePath);
  }
  static private Path of(Path root, String relativePath) {
    assert relativePath.startsWith("/");
    if (instance.virtualFs != null) {
      return instance.virtualFs.getPath(relativePath);
    }
    URI absolutePath = root.resolve(relativePath.substring(1)).toUri();
    return Path.of(absolutePath);
  }

  static public String getAndReadAsset(String path) {
    return read(asset(path));
  }

  static public String read(Path path) {
    return IoErr.of(()->Files.readString(path, StandardCharsets.UTF_8));
  }
  static public Path freshTmpPath(){
    /*var res= Paths.get(
      System.getProperty("java.io.tmpdir"),
      "fearOut"+UUID.randomUUID());*/
    var res = ResolveResource.artefact("/.tmp")
      .resolve("fearOut"+UUID.randomUUID());
    IoErr.of(()->Files.createDirectories(res));
    DeleteOnExit.of(res);
    return res;
  }
}
