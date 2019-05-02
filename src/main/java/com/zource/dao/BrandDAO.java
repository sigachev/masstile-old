package com.zource.dao;

import com.zource.entity.Brand;
import com.zource.form.admin.brand.BrandForm;
import com.zource.model.BrandInfo;
import com.zource.pagination.PaginationResult;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

import static com.zource.utils.HibernateUtils.loadAllData;

@Transactional
@Repository
public class BrandDAO {

    @Autowired
    private SessionFactory sessionFactory;


    public List<Brand> getAllBrands() {

        List<Brand> result = loadAllData(Brand.class, sessionFactory.getCurrentSession());

        return result;
    }


    public Brand getBrandById(Integer id) {
        Brand brand = null;
        EntityManager em = sessionFactory.getCurrentSession().getEntityManagerFactory().createEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Brand> cq = cb.createQuery(Brand.class);
        Root<Brand> Brand = cq.from(com.zource.entity.Brand.class);
        Predicate brandIDPredicate = cb.equal(Brand.get("id"), id);
        cq.where(brandIDPredicate);
        TypedQuery<com.zource.entity.Brand> query = em.createQuery(cq);

        if (query.getResultList().size() > 0)
            brand = query.getSingleResult();

        return brand;
    }

    public List findBrands(String name, String type) {
        EntityManager em = sessionFactory.getCurrentSession().getEntityManagerFactory().createEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Brand> cq = cb.createQuery(Brand.class);

        Root<Brand> brandRoot = cq.from(Brand.class);

        //Constructing list of parameters
        List<Predicate> predicates = new ArrayList<Predicate>();

        predicates.add(cb.like(brandRoot.get("name"), "%" + name + "%"));
        predicates.add(cb.like(brandRoot.get("type"), "%" + type + "%"));


        CriteriaQuery<Brand> query = cq.select(brandRoot).where(predicates.toArray(new Predicate[]{}));
        return em.createQuery(query).getResultList();
    }

    public List getBrandCategories(String id) {
        EntityManager em = sessionFactory.getCurrentSession().getEntityManagerFactory().createEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Brand> cq = cb.createQuery(Brand.class);

        Root<Brand> Brand = cq.from(com.zource.entity.Brand.class);

        Predicate brandNamePredicate = cb.equal(Brand.get("id"), id);
        cq.where(brandNamePredicate);

        TypedQuery<com.zource.entity.Brand> query = em.createQuery(cq);

        com.zource.entity.Brand brand = query.getSingleResult();

        List list = new ArrayList(brand.getBrandCategories());

        return list;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void save(BrandForm brandForm) {

        Session session = this.sessionFactory.getCurrentSession();

        Integer id = brandForm.getId();

        System.out.println("##entered save ");

        Brand brand = null;

        boolean isNew = false;
        if (id != null) {
            brand = this.getBrandById(id);
        }
        if (brand == null) {
            isNew = true;
            brand = new Brand();
            // brand.setCreateDate(new Date());
        }


        //do not set id, otherwise new row in DB will be created
            brand.setName(brandForm.getName());
            brand.setType(brandForm.getType());
            brand.setDescription(brandForm.getDescription());
            brand.setLogo(brandForm.getLogoFileName());


        if (isNew) {
            session.persist(brand);
            System.out.println("## saved " + brand);
        } else {
            session.merge(brand);
            System.out.println("## merged " + brand);
        }
        // If error in DB, Exceptions will be thrown out immediately
        session.flush();
    }


    public PaginationResult<BrandInfo> queryBrands(int page, int maxResult, int maxNavigationPage,
                                                   String likeName) {
        String sql = "Select new " + BrandInfo.class.getName() //
                + "(p.id, p.name) " + " from "//
                + Brand.class.getName() + " p ";
        if (likeName != null && likeName.length() > 0) {
            sql += " Where lower(p.name) like :likeName ";
        }
        /*sql += " order by p.createDate desc ";*/
        //
        Session session = this.sessionFactory.getCurrentSession();
        Query<BrandInfo> query = session.createQuery(sql, BrandInfo.class);

        if (likeName != null && likeName.length() > 0) {
            query.setParameter("likeName", "%" + likeName.toLowerCase() + "%");
        }
        return new PaginationResult<BrandInfo>(query, page, maxResult, maxNavigationPage);
    }


}