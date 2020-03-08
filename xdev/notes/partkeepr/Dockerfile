FROM    debian:buster
MAINTAINER John Yeary <jyeary@bluelotussoftware.com>
SHELL   ["/bin/sh", "-ec"]
ARG     MA_PARTKEEPR_DL_URL=https://downloads.partkeepr.org/partkeepr-1.4.0.tbz2
ARG     MA_DEBIAN_MIRROR=http://192.168.1.16/debian

RUN     set -x; \
	echo deb $MA_DEBIAN_MIRROR buster main > /etc/apt/sources.list; \
	echo deb $MA_DEBIAN_MIRROR buster-updates main \
						>> /etc/apt/sources.list; \
	echo deb http://security.debian.org/ buster/updates main \
						>> /etc/apt/sources.list; \
	apt-get update; \
	apt-get dist-upgrade -y; \
	apt-get install --no-install-recommends -y \
		php7.3 apache2 curl bsdtar cron libapache2-mod-php7.3 \
		php-apcu php7.3-gd php7.3-curl php7.3-bcmath \
		php7.3-ldap php7.3-xml php7.3-mysql php7.3-common \
		php7.3-intl php7.3-opcache patch; \
	apt-get clean; \
	cd /var/www/html; \
	curl --fail -L $MA_PARTKEEPR_DL_URL | \
					bsdtar -xf - --strip-components=1; \
	chown -R www-data:www-data /var/www/html; \
	ls -l /var/www/html; \
	a2enmod rewrite; \
	# https://github.com/partkeepr/PartKeepr/issues/905
	echo LS0tIENvbmZpZ3VyYXRpb24ucGhwCTIwMTgtMDQtMjggMTg6MjU6NDguMDAwMDAwMDAwICswMDAwCisrKyBDb25maWd1cmF0aW9uLnBocAkyMDIwLTAyLTE5IDIxOjU4OjI1LjU2NTQzODg1NyArMDAwMApAQCAtNTkxLDcgKzU5MSw3IEBACiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgLT50aGVuSW52YWxpZCgnU2VydmljZSBoYW5kbGVycyBjYW4gbm90IGhhdmUgYSBmb3JtYXR0ZXIgY29uZmlndXJlZCBpbiB0aGUgYnVuZGxlLCB5b3UgbXVzdCByZWNvbmZpZ3VyZSB0aGUgc2VydmljZSBpdHNlbGYgaW5zdGVhZCcpCiAgICAgICAgICAgICAgICAgICAgICAgICAtPmVuZCgpCiAgICAgICAgICAgICAgICAgICAgICAgICAtPnZhbGlkYXRlKCkKLSAgICAgICAgICAgICAgICAgICAgICAgICAgICAtPmlmVHJ1ZShmdW5jdGlvbiAoJHYpIHsgcmV0dXJuICgnZmluZ2Vyc19jcm9zc2VkJyA9PT0gJHZbJ3R5cGUnXSB8fCAnYnVmZmVyJyA9PT0gJHZbJ3R5cGUnXSB8fCAnZmlsdGVyJyA9PT0gJHZbJ3R5cGUnXSkgJiYgMSAhPT0gY291bnQoJHZbJ2hhbmRsZXInXSk7IH0pCisgICAgICAgICAgICAgICAgICAgICAgICAgICAgLT5pZlRydWUoZnVuY3Rpb24gKCR2KSB7IHJldHVybiAoJ2ZpbmdlcnNfY3Jvc3NlZCcgPT09ICR2Wyd0eXBlJ10gfHwgJ2J1ZmZlcicgPT09ICR2Wyd0eXBlJ10gfHwgJ2ZpbHRlcicgPT09ICR2Wyd0eXBlJ10pICYmIChlbXB0eSgkdlsnaGFuZGxlciddKSB8fCAhaXNfc3RyaW5nKCR2WydoYW5kbGVyJ10pKTsgfSkKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAtPnRoZW5JbnZhbGlkKCdUaGUgaGFuZGxlciBoYXMgdG8gYmUgc3BlY2lmaWVkIHRvIHVzZSBhIEZpbmdlcnNDcm9zc2VkSGFuZGxlciBvciBCdWZmZXJIYW5kbGVyIG9yIEZpbHRlckhhbmRsZXInKQogICAgICAgICAgICAgICAgICAgICAgICAgLT5lbmQoKQogICAgICAgICAgICAgICAgICAgICAgICAgLT52YWxpZGF0ZSgpCg== | base64 -d | patch /var/www/html/vendor/symfony/monolog-bundle/DependencyInjection/Configuration.php;

COPY    php.ini         /etc/php/7.3/apache2/php.ini
COPY    apache.conf     /etc/apache2/sites-available/apache.conf
COPY    parameters.php  /var/www/html/app/config/parameters.php

RUN     a2ensite apache; \
	a2dissite 000-default; \
	chown www-data:www-data /var/www/html/app/config/parameters.php

CMD     ["/bin/sh", "-ec", ". /etc/apache2/envvars; exec /usr/sbin/apache2 -D FOREGROUND"]
