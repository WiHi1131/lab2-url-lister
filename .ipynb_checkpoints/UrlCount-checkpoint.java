import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class UrlCount {

  /** Regex notes:
   *  - (?i) makes "href" case-insensitive.
   *  - Allows optional whitespace around '='.
   *  - Captures content between double quotes (common in Wikipedia HTML dumps).
   *  If your inputs sometimes use single quotes, see the comment below.
   */
  private static final Pattern HREF_PATTERN =
      Pattern.compile("(?i)href\\s*=\\s*\"([^\"]*)\"");

  public static class UrlMapper extends Mapper<Object, Text, Text, IntWritable> {
    private static final IntWritable ONE = new IntWritable(1);
    private final Text outKey = new Text();

    @Override
    public void map(Object key, Text value, Context context)
        throws IOException, InterruptedException {

      String line = value.toString();
      Matcher m = HREF_PATTERN.matcher(line);
      while (m.find()) {
        String url = m.group(1).trim();
        if (!url.isEmpty()) {
          outKey.set(url);
          context.write(outKey, ONE);
        }
      }

      // If you must also handle single quotes, you could either:
      //  (a) run a second matcher with pattern (?i)href\\s*=\\s*'([^']*)'
      //  (b) or use a combined alternation and pick the non-null group.
    }
  }

  /** Combiner to sum partial counts per mapper; safe because sum is assoc/commutative. */
  public static class IntSumCombiner
      extends Reducer<Text, IntWritable, Text, IntWritable> {
    private final IntWritable result = new IntWritable();

    @Override
    public void reduce(Text key, Iterable<IntWritable> values, Context context)
        throws IOException, InterruptedException {
      int sum = 0;
      for (IntWritable v : values) sum += v.get();
      result.set(sum);
      context.write(key, result);
    }
  }

  /** Reducer: sum and filter to only output totals > 5. */
  public static class IntSumReducer
      extends Reducer<Text, IntWritable, Text, IntWritable> {
    private final IntWritable result = new IntWritable();

    @Override
    public void reduce(Text key, Iterable<IntWritable> values, Context context)
        throws IOException, InterruptedException {
      int sum = 0;
      for (IntWritable v : values) sum += v.get();
      if (sum > 5) {
        result.set(sum);
        context.write(key, result);
      }
    }
  }

  public static void main(String[] args) throws Exception {
    if (args.length != 2) {
      System.err.println("Usage: UrlCount <input> <output>");
      System.exit(2);
    }
    Configuration conf = new Configuration();
    Job job = Job.getInstance(conf, "url count");
    job.setJarByClass(UrlCount.class);

    job.setMapperClass(UrlMapper.class);
    job.setCombinerClass(IntSumCombiner.class);
    job.setReducerClass(IntSumReducer.class);

    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(IntWritable.class);

    FileInputFormat.addInputPath(job, new Path(args[0]));
    FileOutputFormat.setOutputPath(job, new Path(args[1]));

    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}
