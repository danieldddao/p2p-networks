class Book < ActiveRecord::Base
  # belongs_to :active_user
  # has_one :active_user

  validates :title, presence: true
  validates :author, presence: true
  validates :location, presence: true

end
