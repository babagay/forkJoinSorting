package ImageDownload;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

/**
 * 30s vs 6s
 */
public class ImageDownloader {

    static long startTime;
    static long endTime;
    static long executionTime;

    static boolean STORE_IMG = false;
    static int[] imgNumbers = {0,1,2,3,4,5,6,7,8,9,10, 11, 12,13,14,15,16,17,18,19,20,
    21,22,23,24,25,26,27,28,29,30, 31,32,33,34,35,36,37,38,39,40, 41,42,43,44,45,46,47,48,49,50};
    // {1};
    // {1,2,3,4,5,6,7,8,9,10};

    public static void main(String[] args) {
        System.out.println("Sequential Image Download started");
        startTime = System.currentTimeMillis();
        SequentialImageDownloader seqDownloader = new SequentialImageDownloader(imgNumbers);
        int res = seqDownloader.downloadAll();
        endTime = System.currentTimeMillis();
        executionTime = endTime - startTime;
        System.out.println("Number of bytes: " + res + ". Time: " + executionTime / 1000.0 + "s");

        System.out.println("Parallel downloader init");
        startTime = System.currentTimeMillis();
        ParallelImageDownloader parallelImageDownloader = new ParallelImageDownloader(imgNumbers, false);
        int numBytes = parallelImageDownloader.downloadAll();
        endTime = System.currentTimeMillis();
        executionTime = endTime - startTime;
        System.out.println("Number of bytes: " + numBytes + ". Time: " + executionTime / 1000.0 + "s");
    }

    static class ParallelImageDownloader {
        private int[] imageNumbers;
        private boolean keepImages = false;
        public ParallelImageDownloader(int[] imageNumbers, boolean storeFiles) {
            this.imageNumbers = imageNumbers;
            keepImages = storeFiles;
        }

        public int downloadAll(){
            return Arrays.stream(imageNumbers).parallel()
                    .map(SequentialImageDownloader::downloadImage)
                    .reduce(0, (u, v) -> {
                        u += v;
                        return u;
                    });
        }
    }

    static class SequentialImageDownloader {

        private int[] imageNumbers;

        public SequentialImageDownloader(int[] imageNumbers) {
            this.imageNumbers = imageNumbers;
        }

        /* returns total bytes from downloading all images in imageNumbers array */
        public int downloadAll() {
            int totalBytes = 0;
            for (int num : imageNumbers)
                totalBytes += downloadImage(num);
            return totalBytes;
        }

        /* returns number of bytes from downloading image */
        public static int downloadImage(int imageNumber) {
            BufferedInputStream in = null;
            FileOutputStream out = null;
            String fileName = "";
            try {
                imageNumber = (Math.abs(imageNumber) % 50) + 1; // force number between 1 and 50
                URL photoURL = new URL(String.format("http://699340.youcanlearnit.net/image%03d.jpg", imageNumber));
                in = new BufferedInputStream(photoURL.openStream());
                int bytesRead, totalBytes = 0;
                byte buffer[] = new byte[1024];

                if (STORE_IMG) {
                    fileName = "image" + imageNumber + ".jpg";
                    out = new FileOutputStream(fileName);
                }

                while((bytesRead = in.read(buffer, 0, 1024)) != -1) {
                    totalBytes += bytesRead;
                    if (STORE_IMG) out.write(buffer, 0, bytesRead);
                }

                System.out.println("Image fetched: " + imageNumber);

                if (STORE_IMG) {
                    out.close();
                    System.out.println("Image stored: " + fileName);
                }

                return totalBytes;
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                    try {
                        in.close();
                        out.close();
                    } catch (Throwable e) {
                        // throw new RuntimeException(e);
                    }
            }
            return 0;
        }
    }
}
