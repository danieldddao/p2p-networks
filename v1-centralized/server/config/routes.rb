Rails.application.routes.draw do
  root 'books#index'
  get 'books/index'

  post 'connect', to: 'users#connect'
  post 'books/new', to: 'books#create'
  post 'books/search', to: 'books#search'
  post 'books/allmybooks', to: 'books#getAllMyBooks'
  post 'books/updatelocation', to: 'books#updateLocation'
  post 'books/updatesharingstatus', to: 'books#updateSharingStatus'
  post 'books/unsharebook', to: 'books#unshareBook'

  post 'users/create', to: 'users#create'
  post 'users/login', to: 'users#login'
  post 'users/logout', to: 'users#logout'

end
