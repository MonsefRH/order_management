Order management (using Thread,FileReader,JDBC )
--

Tables :

CREATE TABLE `customer` (
  `id` int(11) NOT NULL,
  `nom` varchar(100) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `phone` varchar(15) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


INSERT INTO `customer` (`id`, `nom`, `email`, `phone`) VALUES
(1, 'Monsef Rh1', 'Monsef@example.com', '0123456789'),
(2, 'Ahmed ', 'Ahmed@example.com', '0987654321'),
(3, 'Taha', 'Tahac@example.com', '0567890123');


Table structure for table `orders`

CREATE TABLE `orders` (
  `id` int(11) NOT NULL,
  `date` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `amount` decimal(10,2) DEFAULT NULL,
  `customer_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
