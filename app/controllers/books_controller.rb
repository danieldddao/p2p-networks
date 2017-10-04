class BooksController < ApplicationController
  require 'socket'

  def index
    puts "index page:"
    public_id = request.remote_ip
    local_ip = Socket.ip_address_list.detect{|intf| intf.ipv4_private?}
    puts local_ip.ip_address
  end

  def search

  end

end