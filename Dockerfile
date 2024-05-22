FROM --platform=linux/arm64/v8 ubuntu:20.04

RUN apt-get update
RUN apt-get -y install openjdk-8-jdk wget

# install Maven
RUN wget https://mirrors.estointernet.in/apache/maven/maven-3/3.6.3/binaries/apache-maven-3.6.3-bin.tar.gz && tar -xvf apache-maven-3.6.3-bin.tar.gz && mv apache-maven-3.6.3 /opt/ && M2_HOME='/opt/apache-maven-3.6.3' && PATH="$M2_HOME/bin:$PATH" && export PATH

# install node
RUN apt-get install -y nodejs npm python2
RUN apt-get -y install build-essential chrpath libssl-dev libxft-dev
RUN apt-get -y install libfreetype6 libfreetype6-dev
RUN apt-get install -y libfontconfig1 libfontconfig1-dev

# phantomjs needs to be selected based on os
#RUN export PHANTOM_JS="phantomjs-2.1.1-linux-x86_64" && wget https://bitbucket.org/ariya/phantomjs/downloads/$PHANTOM_JS.tar.bz2 && tar xvjf $PHANTOM_JS.tar.bz2 && mv $PHANTOM_JS /usr/local/share && ln -sf /usr/local/share/$PHANTOM_JS/bin/phantomjs /usr/local/bin
RUN wget https://launchpad.net/ubuntu/+source/phantomjs/2.1.1+dfsg-2ubuntu1/+build/19118060/+files/phantomjs_2.1.1+dfsg-2ubuntu1_arm64.deb
RUN apt install -y ./phantomjs_2.1.1+dfsg-2ubuntu1_arm64.deb
RUN npm install -g bower
RUN apt-get -y install git
# for some reason using git, installing bower gets a connection refused error
RUN git config --global url."https://".insteadOf git://

# this might only be necessary for macos
ENV QT_QPA_PLATFORM offscreen
ENV PATH="${PATH}:/opt/apache-maven-3.6.3/bin/"