class CreateBooks < ActiveRecord::Migration[5.1]
  def change
    create_table :books do |t|
      t.string :user_ip
      t.string :port_number
      t.string :title
      t.string :author
      t.string :isbn
      t.string :location
      t.boolean :isShared, :default => true
      t.index ["user_ip", "port_number", "location"], name: "user_ip_and_port_and_location", unique: true
      # t.index ["user_client_ip", "user_ip", "title", "author", "location"], name: "user_client_ip_and_user_ip_and_title_and_author_and_location", unique: true
    end
  end
end
