INSERT INTO event.events (title, event_date, description)
SELECT v.title, v.event_date, v.description
FROM (VALUES
    ('Alumni Networking Night', '2025-06-10', 'Meet fellow alumni and expand your professional network.'),
    ('UL Career Fair 2025', '2025-07-15', 'Connect with employers and explore career opportunities.'),
    ('Graduation Ceremony 2025', '2025-08-01', 'Celebrate the class of 2025 at the annual graduation ceremony.')
) AS v(title, event_date, description)
WHERE NOT EXISTS (SELECT 1 FROM event.events LIMIT 1);
