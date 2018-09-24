package br.com.willianantunes.services;

import br.com.willianantunes.domain.Product;
import br.com.willianantunes.domain.User;
import br.com.willianantunes.repositories.UserRepository;
import br.com.willianantunes.services.dtos.ProductDTO;
import br.com.willianantunes.services.mappers.ProductMapper;
import br.com.willianantunes.support.ScenarioBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @SpyBean
    private ProductMapper productMapper;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ScenarioBuilder scenarioBuilder;


    @BeforeEach
    void init() throws IOException {

        scenarioBuilder.createMockedUsersOnDatabase();
    }

    @AfterEach
    void tearDown() {

        scenarioBuilder.clearAllRepositories();
    }

    @Test
    @DisplayName("Should bind a product to an existing user")
    public void a() {

        ProductDTO productDTO = ProductDTO.builder()
            .code("07891167021013")
            .details("A huge salt shaker for your every day use")
            .name("Huge Salt Shaker")
            .price(new BigDecimal("25.00"))
            .imageLink("https://cdn.pixabay.com/photo/2018/04/02/20/17/salt-3285024_960_720.jpg").build();

        User jafar = userRepository.findOneByEmail("jafar@agrabah.com").orElseThrow();

        productService.bindProductToUser(productDTO, jafar.getId().toString());

        verify(productMapper).productDTOToProduct(eq(productDTO));
        List<Product> currentProductsOfJafar = userRepository.findById(jafar.getId()).get().getProducts();
        assertThat(currentProductsOfJafar).hasSize(jafar.getProducts().size() + 1);
    }

    @Test
    @DisplayName("Should delete a product from an existing user")
    public void b() {

        User jafar = userRepository.findOneByEmail("jafar@agrabah.com").orElseThrow();
        Product anyProduct = jafar.getProducts().stream().findAny().orElseThrow();

        productService.deleteProductFromUser(anyProduct.getId(), jafar.getId().toString());

        List<Product> currentProductsOfJafar = userRepository.findById(jafar.getId()).get().getProducts();
        assertThat(currentProductsOfJafar).hasSize(jafar.getProducts().size() - 1);
    }

    @Test
    @DisplayName("Should update a product from an existing user")
    public void c() {

        User jafar = userRepository.findOneByEmail("jafar@agrabah.com").orElseThrow();
        Product anyProduct = jafar.getProducts().stream().findAny().orElseThrow();

        ProductDTO productDTO = productMapper.productToProductDTO(anyProduct);
        productDTO.setName("UPDATED FOO NAME");
        productDTO.setPrice(new BigDecimal("69.00"));

        productService.updateProductFromUser(anyProduct.getId(), productDTO, jafar.getId().toString());

        verify(productMapper).productDTOToProduct(eq(productDTO), eq(anyProduct.getId()));
        User updatedJafar = userRepository.findOneByEmail("jafar@agrabah.com").orElseThrow();
        assertThat(updatedJafar.getProducts().stream().filter(p -> p.getId().equals(anyProduct.getId())).findAny().orElseThrow())
            .extracting(p -> p.getName(), p -> p.getPrice())
            .containsExactly(productDTO.getName(), productDTO.getPrice());
    }

    @Test
    @DisplayName("Should return all the products from an existing user")
    public void d() {

        User jafar = userRepository.findOneByEmail("jafar@agrabah.com").orElseThrow();

        List<Product> jafarProducts = productService.getAllProductsFromUser(jafar.getId().toString());

        assertThat(jafarProducts).hasSize(jafar.getProducts().size());
    }
}