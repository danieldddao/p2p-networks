Rails.application.routes.draw do
  get 'books/index'
  get 'books/new'
  post 'books/new', to: 'books#create'
  root 'books#index'
  post 'books/search', to: 'books#search'
end
