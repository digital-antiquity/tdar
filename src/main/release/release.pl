#!/usr/bin/perl

require Mail::Send;
my $hostname = `cat /etc/hostname`;
$hostname =~ s/[\r\n]//ig;
my $svnversion = `hg id -n`;
$svnversion =~ s/[\r\n]//ig;
print "Deploying Version: " . $svnversion ."\r\n";

my  $msg = Mail::Send->new;
  $msg->to('tdar-dev@LISTS.ASU.EDU, abrin@asu.edu');
  $msg->subject($ENV{'USER'} . ' restarted tDAR on ' . $hostname . ' [' . $svnversion . ']');
  $msg->set('From','release@tdar.org');
my $fh = $msg->open;
my $date = scalar(localtime);
print $fh "Date: $date \r\n";
print $fh "Host: $hostname \r\n";
print $fh "Current Verson:  $svnversion \r\n";
$fh->close   or die "couldn't send whole message: $!\n";
`echo '$ENV{'USER'} released $svnversion $date' >> /home/tdar/release.log`