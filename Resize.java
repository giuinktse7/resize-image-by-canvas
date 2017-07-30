import javax.imageio.ImageIO;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.awt.geom.AffineTransform;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.stream.Stream;
import java.util.List;
import java.util.ArrayList;
import static java.util.stream.Collectors.toList;
import java.io.Console;

public class Resize {
  private static String[] acceptedImageTypes = {"png", "jpg", "jpeg"};
  private static List<String> resizedFilePaths;
  private static final String RESIZE_FOLDER_PATH = "./resized-images/";
  private static final double ASPECT_RATIO = 0.75;

  public static void main(String[] args) {
      createResizedFolder();
      loadResizedFiles();

      System.out.println("Resizing images...");
      File folder = new File("./images");
      Stream.of(folder.listFiles())
            .filter(Resize::shouldBeResized)
            .forEach(Resize::processImage);

      System.out.println("Done.");

      Console c = System.console();
      if (c != null) {
          c.format("\nPress ENTER to exit.\n");
          c.readLine();
      }
  }

  private static void loadResizedFiles() {
    File resizedFolder = new File(RESIZE_FOLDER_PATH);
    try {
      resizedFilePaths = Stream.of(resizedFolder.listFiles())
            .map(file -> file.getName())
            .collect(toList());
    } catch(Exception e) {
      resizedFilePaths = new ArrayList<>();
    }
  }

  private static void processImage(File imageFile) {
    try {
      BufferedImage image = ImageIO.read(imageFile);

      double quotient = image.getWidth() / image.getHeight();
      int newWidth = quotient < ASPECT_RATIO ? (int) (image.getHeight() * 3 / 4) : image.getWidth();
      int newHeight = quotient > ASPECT_RATIO ? (int) (image.getWidth() * 4 / 3) : image.getHeight();
      BufferedImage scaled = Resize.resizeImage(image, newWidth, newHeight);
      Resize.save(scaled, imageFile.getName());
    } catch (IOException e) {
    }
  }

  private static boolean isImage(File file) {
    String path = file.getName();
    String fileEnding = path.substring(path.lastIndexOf('.') + 1);
    return Stream.of(acceptedImageTypes).anyMatch(x -> x.equals(fileEnding.toLowerCase()));
  }

  private static boolean isResized(File file) {
    return resizedFilePaths.contains(file.getName());
  }

  private static boolean shouldBeResized(File file) {
    return isImage(file) && !isResized(file);
  }

  private static void createResizedFolder() {
    File folder = new File("./" + RESIZE_FOLDER_PATH);

    if (!folder.exists()) {
      try {
        folder.mkdir();
      } catch (SecurityException se) {
        System.out.println("No privilege to create folder: " + RESIZE_FOLDER_PATH);
      }
    }
  }

  private static void save(RenderedImage image, String imagePath) throws IOException {
    System.out.println(imagePath + " resized.");
    ImageIO.write(image, "png", new File(RESIZE_FOLDER_PATH + imagePath));
  }

  public static BufferedImage resizeImage(BufferedImage sourceImage, Integer newWidth, Integer newHeight) {
    int srcWidth = sourceImage.getWidth();
    int srcHeight = sourceImage.getHeight();
    BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2d = resizedImage.createGraphics();
    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    g2d.drawImage(sourceImage, (newWidth - srcWidth) / 2, (newHeight - srcHeight) / 2, srcWidth, srcHeight, null);
    g2d.dispose();
    return resizedImage;
  }

}
