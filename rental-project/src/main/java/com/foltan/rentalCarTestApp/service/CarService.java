package com.foltan.rentalCarTestApp.service;

import com.foltan.rentalCarTestApp.domain.Car;
import com.foltan.rentalCarTestApp.domain.CarPackage;
import com.foltan.rentalCarTestApp.domain.CarParameters;
import com.foltan.rentalCarTestApp.dto.CarDto;
import com.foltan.rentalCarTestApp.dto.CarPackageDto;
import com.foltan.rentalCarTestApp.repository.CarPackageRepository;
import com.foltan.rentalCarTestApp.repository.CarParametersRepository;
import com.foltan.rentalCarTestApp.repository.CarRepository;
import com.foltan.rentalCarTestApp.exception.ExistingEntityException;
import com.foltan.rentalCarTestApp.utils.PageValidator;
import com.foltan.rentalCarTestApp.utils.SortValidator;
import com.foltan.rentalCarTestApp.mapper.CarDtoMapper;
import com.foltan.rentalCarTestApp.mapper.CarPackageDtoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CarService {

        public static final int DEFAULT_PAGE_SIZE = 10;
        private final CarRepository carRepository;
        private final CarPackageRepository carPackageRepository;
        private final CarParametersRepository carParametersRepository;

        public List<Car> getAllCars(Integer page, Sort.Direction sort) {
                log.info("Fetching all cars");
                int pageNumber = PageValidator.pageNumber(page);
                Sort.Direction sortDirection = SortValidator.sortDirection(sort);
                return carRepository.findCars(PageRequest.of(pageNumber - 1, DEFAULT_PAGE_SIZE, Sort.by(sortDirection, "id")));
        }

        public List<CarPackage> getCarPackages() {
                log.info("Fetching all car packages");
                return carPackageRepository.findAll();
        }

        public List<Car> getAvailableCars(Integer page, Sort.Direction sort) {
                log.info("Fetching available cars");
                int pageNumber = PageValidator.pageNumber(page);
                Sort.Direction sortDirection = SortValidator.sortDirection(sort);
                return carRepository.findAvailableCars(PageRequest.of(pageNumber - 1, DEFAULT_PAGE_SIZE, Sort.by(sortDirection, "id")));
        }

        public Car getCar(Long id) {
                log.info("Fetching car with id {}", id);
                return carRepository.findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("Car With This ID Does Not Exists!"));
        }

        public Car saveCar(CarDto carDto) {
                log.info("Saving new car {} {} to the database", carDto.getBrand(), carDto.getModel());
                Car car = CarDtoMapper.mapToCar(carDto);
                return carRepository.save(car);
        }

        public Car editCar(Long id, CarDto carDto) {
                Car carEdited = carRepository.findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("This Car Does Not Exists!"));
                log.info("Edition car with id {}", id);
                carEdited.setRegistrationNr(carDto.getRegistrationNr());
                carEdited.setBrand(carDto.getBrand());
                carEdited.setModel(carDto.getModel());
                carEdited.setIsAvailable(carDto.getIsAvailable());
                return carRepository.save(carEdited);
        }

        public Car setCarParameters(Long id, CarParameters carParameters) {
                Car car = carRepository.findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("This Car Does Not Exists!"));
                log.info("Setting parameters of car with id {}", id);
                if(car.getCarParameters() != null) {

                        carParametersRepository.delete(car.getCarParameters());
                }
                carParametersRepository.save(carParameters);
                car.setCarParameters(carParameters);
                return carRepository.save(car);
        }

        public Car setCarPackage(Long id, String packageName) {
                Car car = carRepository.findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("This Car Does Not Exists!"));
                CarPackage carPackage = carPackageRepository.findByPackageName(packageName)
                        .orElseThrow(() -> new EntityNotFoundException("This Package Does Not Exists!"));
                log.info("Setting package of car with id {}", id);
                car.setCarPackage(carPackage);
                return carRepository.save(car);
        }

        public void deleteCar(Long id) {
                log.info("Deleting car with id {}", id);
                if(!carRepository.existsById(id)) {

                        throw new EntityNotFoundException("This Car Does Not Exists!");
                }
                carRepository.deleteById(id);
        }

        public CarPackage saveCarPackage(CarPackageDto carPackageDto) {
                if(carPackageRepository.findByPackageName(carPackageDto.getPackageName()).isPresent()) {

                        throw new ExistingEntityException("This Package Already Exists!");
                }
                log.info("Saving new package {} to the database", carPackageDto.getPackageName());
                return carPackageRepository.save(CarPackageDtoMapper.mapToCarPackage(carPackageDto));
        }

        public void deleteCarPackage(Long id) {
                log.info("Deleting car package with id {}", id);
                if(!carPackageRepository.existsById(id)) {

                        throw new EntityNotFoundException("This Package Does Not Exists!");
                }
                CarPackage carPackage = carPackageRepository.getById(id);
                Collection<Car> cars = carPackage.getCars();
                for (Car car : cars) {
                        car.setCarPackage(null);
                }
                carPackageRepository.delete(carPackage);
        }

}
