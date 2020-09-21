#!/usr/bin/perl

### http://www.iburiworks.com/petittools/slp.html

$dir = "selenium/tests";
$slp = "tests.slp";
$ext = "htm";

# Headers and footers

$test_header = <<EOH;	# Header of TestCase
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<meta http-equiv="Cache-Control" content="no-cache">
	<title>$test</title>
</head>
<body>
	<table cellpadding="1" cellspacing="1" border="1">
		<tbody>
EOH

$test_footer = <<EOH;	# Footer of TestCase
		</tbody>
	</table>
</body>
</html>
EOH

$test_suite_header = <<EOH;	# Header of TestSuite
<html>
<head>
	<meta content="text/html" http-equiv="content-type">
	<title>Test Suite</title>
</head>
<body>
	<table cellpadding="1" cellspacing="1" border="1">
		<tbody>
			<tr><td><b>Test Suite</b></td></tr>
EOH

$test_suite_footer = <<EOH;	# Footer of TestSuite
		</tbody>
	</table>
</body>
</html>
EOH

# Initialize
$test = "Test";
$opened = 0;

# Open selenium processor file.
open SEL, "<$slp";
while(<SEL>) {
	chomp;
	if(/\*(.+)/) {
		# New TestCase
		# Write footer and close if opened
		if($opened) {
			print HTML $test_footer;
			close HTML;
		}
		$test = $1;
		$opened = 0;
	} elsif(/^#(.*)/ || /^$/) {
		# Comment or blank: ignored
	} else {
		# Command in TestCase
		if(!$opened) {
			# Open and write header
			open HTML, ">$dir/$test.$ext";
			print HTML $test_header;
			print HTML <<EOH;
			<tr>
				<td colspan="3">$test</td>
			</tr>
EOH

			# Push to TestCase array
			push @tests, $test;
			$opened = 1;
		}

		# Split as command and write them
		@commands = split(/\t/);
		for($i=0;$i<3;$i++) {
			# Initialize as &nbsp;
			$commands[$i] = "&nbsp;" unless $commands[$i];
		}
		print HTML <<EOH;
			<tr>
				<td>$commands[0]</td>
				<td>$commands[1]</td>
				<td>$commands[2]</td>
			</tr>
EOH
	}
}

# Write footer and close if opened
if($opened) {
	print HTML $test_footer;
	close HTML;
}

# Close selenium processor file
close SEL;

# Write TestSuite
open HTML, ">$dir/TestSuite.$ext";
print HTML $test_suite_header;
foreach(@tests) {
	print HTML <<EOH;
			<tr><td><a href="./$_.$ext">$_</a></td></tr>
EOH
}
print HTML $test_suite_footer;
close HTML;

