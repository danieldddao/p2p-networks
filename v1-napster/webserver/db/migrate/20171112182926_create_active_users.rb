class CreateActiveUsers < ActiveRecord::Migration[5.1]
  def change
    create_table :active_users do |t|
      t.string :username
      t.string :user_ip
      t.string :port_number
    end
    add_index :active_users, :username, unique:true
  end
end
