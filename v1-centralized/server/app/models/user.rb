class User < ActiveRecord::Base
    # before_create :confirmation_token

    has_secure_password
    before_save {|user| user.username=user.username.downcase}
    # before_save :create_session_token

    # private
    # def create_session_token
    #     self.session_token = SecureRandom.urlsafe_base64
    # end
    # def confirmation_token
    #   if self.confirm_token.blank?
    #       self.confirm_token = SecureRandom.urlsafe_base64.to_s
    #   end
    # end
    
end
