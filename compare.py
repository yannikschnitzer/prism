def compare(file1, file2):
    """
    Compare two text files line by line and print the differences.
    :param file1: Path to the first file
    :param file2: Path to the second file
    :return: None
    """
    with open(file1, 'r') as f1, open(file2, 'r') as f2:
        lines1 = f1.readlines()
        lines2 = f2.readlines()

    # Compare the number of lines first
    if len(lines1) != len(lines2):
        print(f"Files have different number of lines: {len(lines1)} vs {len(lines2)}")

    # Compare lines one by one
    differences = []
    for i, (line1, line2) in enumerate(zip(lines1, lines2), 1):
        if line1 != line2:
            differences.append((i, line1.strip(), line2.strip()))

    # Print the differences, if any
    if differences:
        print(f"Found {len(differences)} difference(s):")
        for line_number, line1, line2 in differences:
            print(f"Line {line_number}:\n  File1: {line1}\n  File2: {line2}\n")
    else:
        print("Files are identical.")

# Example usage:
# compare_files("file1.txt", "file2.txt")

compare('Fix.txt', "nonFix.txt")