class UsersController < ApplicationController

  def create
    user = User.new(:username => params[:username], :password => params[:password])
    if user.save
      render json: user, status: 201
      puts "created new user"
    else
      render json: user, status: 208
      puts "can't create new user"
    end
  end

  def login
    user = User.find_by_username(params[:username])
    if user && user.authenticate(params[:password])
      # Add to active users table
      activeUser = ActiveUser.new(:username => params[:username], :user_ip => params[:user_ip], :port_number => params[:port_number])
      puts "logging in"
      if activeUser.save
        render json: activeUser, status: 200
        puts "user" + activeUser.username + " logged in"
      else
        render json: user, status: 208
        puts "already logged in"
      end
    else
      render json: user, status: 204
      puts "username doesn't exist"
    end
  end

  def logout
    # Remove from active users table
    user = ActiveUser.where(:username => params[:username])
    if user.destroy_all
      render json: user, status: 200
      puts "user logged out"
    else
      render json: user, status: 204
      puts "can't log out"
    end
  end

end
