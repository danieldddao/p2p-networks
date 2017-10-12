Rails.application.routes.draw do
  root 'books#index'
  get 'books/index'

  post 'books/new', to: 'books#create'
  post 'books/search', to: 'books#search'

  post 'books/clear', to: 'books#clear'
  post 'books/delete', to: 'books#destroy'
end
