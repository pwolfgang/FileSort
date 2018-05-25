# Method to sort a large files.

This method sorts a large text file.  If the file can be read into memory,
it is sorted using the Java Collections::sort method.  If not, the largest
possible chunks are read, sorted, and then written to temporary files. The
contents of the temporary files are then merged.
