INSERT INTO categories (name,description,created_at,updated_at) VALUES
('Smartphones','Wholesale and retail smartphone enquiries',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
('Mobile Covers','Designer and protective phone covers',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
('D+ Glass','Screen protection and tempered glass',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
('Smart Watches','Smart watches and wearable accessories',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
('Chargers','Charging adapters',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
('Earphones','Wired earphones',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
('Bluetooth Earbuds','Wireless earbuds',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
('Neckbands','Bluetooth neckbands',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
('Power Banks','Portable charging accessories',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
('Data Cables','Charging and data cables',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
('Car Chargers','In-car charging accessories',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
('Bluetooth Speakers','Portable speakers',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
('Memory Cards','Memory cards',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
('Pen Drives','USB storage',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
('Mobile Holders','Phone stands and holders',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
('Gift Items','Gift enquiries',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
('Other Accessories','Other mobile-related products',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);

INSERT INTO website_settings (id,business_name,phone,whatsapp,email,address,business_hours,tagline,announcement,created_at,updated_at) VALUES
(1,'Sinnar Mobile','7620050500','917620050500','sinnarmobile0@gmail.com',
'Sinnar Mobile, Shop No. 1, Opp. Hiralal Tower, Near Sinnar Bus Stand, Sinnar, Maharashtra 422103',
'Please call to confirm today''s opening hours.',
'Everything Your Mobile Needs – Under One Roof!',
'Smartphone offers, wholesale enquiries & everyday essentials. WhatsApp us for today''s details.',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);

INSERT INTO faqs (question,answer,display_order,created_at,updated_at) VALUES
('Can I buy a new smartphone?','Yes. We deal in smartphones for retail and wholesale buyers. Contact us for current models, prices and availability.',1,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
('Do you accept wholesale enquiries?','Yes. Share the models and quantities you need on WhatsApp and we will discuss your requirements.',2,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
('How can I check the latest offers?','Visit our Offers page or message us on WhatsApp. Prices and offer terms are confirmed by the shop.',3,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
('Which payment methods can I use?','Please contact the shop to confirm currently accepted payment methods before your visit.',4,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
('Do products come with a warranty?','Warranty depends on the product, brand and applicable terms. Please ask for the details before purchasing.',5,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
('Can I get my phone repaired?','We take enquiries for screen, battery, charging port, audio, software and other repairs. Diagnosis determines the available repair options.',6,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
('How long does a repair take?','Timing depends on the issue and parts availability. We will discuss an estimate after inspecting your device.',7,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
('Do you sell covers for my phone?','Share your exact phone model so we can check suitable designer and protective covers.',8,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
('Can I enquire about tempered glass and accessories?','Yes. We offer D+ glass and mobile accessories. Contact us to check compatibility and availability.',9,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
('Where is Sinnar Mobile?','Visit Shop No. 1, Opp. Hiralal Tower, Near Sinnar Bus Stand, Sinnar, Maharashtra 422103. Call 7620050500 for directions.',10,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);
