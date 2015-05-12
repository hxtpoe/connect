include 'erlang'

package { 'erlang-base':
  ensure => 'latest',
}

apt::source { 'apt-source':
  location => 'http://www.rabbitmq.com/debian/',
  release => 'testing',
  repos => 'main',
  include_src => false,
  key        => 'F7B8CEA6056E8E56',
  key_source => 'http://www.rabbitmq.com/rabbitmq-signing-key-public.asc',
}

class { 'rabbitmq':
  require => Apt::Source['apt-source'],
  service_manage    => false,
  port              => '5672',
  admin_enable      => true,
  delete_guest_user => true,
}

rabbitmq_plugin {'rabbitmq_management':
  ensure => present,
}

rabbitmq_user { 'dan':
  admin    => true,
  password => 'bar',
}

rabbitmq_vhost { 'myhost2':
  ensure => present,
}
